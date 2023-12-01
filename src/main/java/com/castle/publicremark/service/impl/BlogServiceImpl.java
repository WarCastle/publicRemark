package com.castle.publicremark.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.Blog;
import com.castle.publicremark.entity.User;
import com.castle.publicremark.mapper.BlogMapper;
import com.castle.publicremark.service.IBlogService;
import com.castle.publicremark.service.IUserService;
import com.castle.publicremark.utils.SystemConstants;
import com.castle.publicremark.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.castle.publicremark.utils.RedisConstants.BLOG_LIKED_KEY;

/**
 * @author YuLong
 * Date: 2022/11/16 20:18
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
        Long userId = UserHolder.getUser().getId();
        // 2.判断当前登录用户是否已经点赞
        String blogKey = BLOG_LIKED_KEY + blog.getId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(blogKey, userId.toString());
        blog.setIsLike(BooleanUtil.isTrue(isMember));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result likeBlog(Long id) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.判断当前登录用户是否已经点赞
        String blogKey = BLOG_LIKED_KEY + id;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(blogKey, userId.toString());
        if (BooleanUtil.isFalse(isMember)) {
            // 3.如果未点赞，可以点赞
            // 3.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2.保存用户到Redis的set集合
            if (isSuccess) {
                stringRedisTemplate.opsForSet().add(blogKey, userId.toString());
            }
        } else {
            // 4.如果已点赞，取消点赞
            // 4.1.数据库点赞数 - 1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2.把用户从Redis的set集合中移除
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(blogKey, userId.toString());
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
}
