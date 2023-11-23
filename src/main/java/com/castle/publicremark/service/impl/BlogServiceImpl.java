package com.castle.publicremark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.entity.Blog;
import com.castle.publicremark.mapper.BlogMapper;
import com.castle.publicremark.service.IBlogService;
import org.springframework.stereotype.Service;

/**
 * @author YuLong
 * Date: 2022/11/16 20:18
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
}
