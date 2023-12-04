package com.castle.publicremark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.Blog;

/**
 * @author YuLong
 * Date: 2022/11/16 19:59
 */
public interface IBlogService extends IService<Blog> {
    Result saveBlog(Blog blog);

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result queryBlogOfFollow(Long max, Integer offset);
}
