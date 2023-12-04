package com.castle.publicremark.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.dto.ScrollResult;
import com.castle.publicremark.dto.UserDTO;
import com.castle.publicremark.entity.Blog;
import com.castle.publicremark.entity.Follow;
import com.castle.publicremark.entity.User;
import com.castle.publicremark.mapper.BlogMapper;
import com.castle.publicremark.service.IBlogService;
import com.castle.publicremark.service.IFollowService;
import com.castle.publicremark.service.IUserService;
import com.castle.publicremark.utils.SystemConstants;
import com.castle.publicremark.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.castle.publicremark.utils.RedisConstants.*;

/**
 * @author YuLong
 * Date: 2022/11/16 20:18
 */
@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveBlog(Blog blog) {
        stringRedisTemplate.setEnableTransactionSupport(true);
        try {
            // 1.获取登录用户
            UserDTO user = UserHolder.getUser();
            blog.setUserId(user.getId());
            // 2.保存探店博文
            boolean isSuccess = save(blog);
            if (!isSuccess) {
                return Result.fail("新增笔记失败！");
            }
            // 3.查询笔记作者的所有粉丝 select * from tb_follow where follow_user_id = ?
            List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
            // 4.推送笔记id给所有粉丝
            stringRedisTemplate.multi();
            follows.forEach(follow -> {
                // 4.1.获取粉丝id
                Long userId = follow.getUserId();
                // 4.2.推送
                String feedKey = USER_FEED_KEY + userId;
                stringRedisTemplate.opsForZSet().add(feedKey, blog.getId().toString(), System.currentTimeMillis());
            });
            stringRedisTemplate.exec();
        } catch (Exception e) {
            stringRedisTemplate.discard();
            logger.error("发笔记推送到粉丝收件箱失败 : {}", e.getMessage());
            throw new RuntimeException("发笔记推送到粉丝收件箱失败！");
        }
        // 5.返回id
        return Result.success(blog.getId());
    }

    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> pageInfo = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = pageInfo.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return Result.success(records);
    }

    @Override
    public Result queryBlogById(Long id) {
        // 1.查询blog
        Blog blog = getById(id);
        if (Objects.isNull(blog)) {
            return Result.fail("笔记不存在！");
        }
        // 2.查询blog有关的用户
        queryBlogUser(blog);
        // 3.查询blog是否被点赞
        isBlogLiked(blog);
        return Result.success(blog);
    }

    private void isBlogLiked(Blog blog) {
        // 1.获取登录用户
        UserDTO user = UserHolder.getUser();
        if (Objects.isNull(user)) {
            // 用户未登录，无需查询是否点赞
            return;
        }
        Long userId = user.getId();
        // 2.判断当前登录用户是否已经点赞
        String blogKey = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(blogKey, userId.toString());
        blog.setIsLike(Objects.nonNull(score));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result likeBlog(Long id) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.判断当前登录用户是否已经点赞
        String blogKey = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(blogKey, userId.toString());
        if (Objects.isNull(score)) {
            // 3.如果未点赞，可以点赞
            // 3.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2.保存用户到Redis的ZSet集合 zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(blogKey, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.如果已点赞，取消点赞
            // 4.1.数据库点赞数 - 1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2.把用户从Redis的set集合中移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(blogKey, userId.toString());
            }
        }
        return Result.success();
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 1.查询top5的点赞用户 zrange key 0 4
        Set<String> range = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (Objects.isNull(range) || range.isEmpty()) {
            return Result.success();
        }
        // 2.解析出其中的用户id
        List<Long> ids = range.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        // 3.根据用户id查询用户 WHERE id IN (5, 1) ORDER BY FIELD(id, 5, 1)
        List<UserDTO> userDtoList = userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        // 4.返回
        return Result.success(userDtoList);
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        // 1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 2.查询收件箱 ZREVRANGEBYSCORE key Min Max LIMIT offset count
        String feedKey = USER_FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(feedKey, 0, max, offset, 10);
        // 3.非空判断
        if (Objects.isNull(typedTuples) || typedTuples.isEmpty()) {
            return Result.success();
        }
        // 4.解析数据（blogId、score(时间戳)、offset）
        List<Long> ids = new ArrayList<>(typedTuples.size());
        // 这里的minTime[0]和os[0]等效于minTime和os，Lambda表达式不可修改表达式外部变量，变量只能修饰为final，因此只能使用数组
        final long[] minTime = {0};
        final int[] os = {1};
        typedTuples.forEach(tuple -> { // 5 4 4 2 2
            // 4.1.获取id
            ids.add(Long.valueOf(Objects.requireNonNull(tuple.getValue())));
            // 4.2.获取分数（时间戳）
            long time = Objects.requireNonNull(tuple.getScore()).longValue();
            if (Objects.equals(time, minTime[0])) {
                os[0]++;
            } else {
                minTime[0] = time;
                os[0] = 1;
            }
        });
        // 5.根据id查询blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        blogs.forEach(blog -> {
            // 查询blog有关的用户
            queryBlogUser(blog);
            // 查询blog是否被点赞
            isBlogLiked(blog);
        });
        // 6.封装并返回
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogs);
        scrollResult.setOffset(os[0]);
        scrollResult.setMinTime(minTime[0]);
        return Result.success(scrollResult);
    }
}
