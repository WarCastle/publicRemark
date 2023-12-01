package com.castle.publicremark.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.dto.UserDTO;
import com.castle.publicremark.entity.Blog;
import com.castle.publicremark.entity.User;
import com.castle.publicremark.service.IBlogService;
import com.castle.publicremark.service.IUserService;
import com.castle.publicremark.utils.SystemConstants;
import com.castle.publicremark.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuLong
 * Date: 2022/11/17 11:42
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        // 获取登录用户
        UserDTO userDTO = UserHolder.getUser();
        blog.setUserId(userDTO.getId());
        // 保存探店博文
        blogService.save(blog);
        // 返回id
        return Result.success(blog.getId());
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO userDTO = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> pageInfo = blogService.query()
                .eq("user_id", userDTO.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = pageInfo.getRecords();
        return Result.success(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }
}
