package com.castle.publicremark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.entity.BlogComments;
import com.castle.publicremark.mapper.BlogCommentsMapper;
import com.castle.publicremark.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * @author YuLong
 * Date: 2022/11/16 19:14
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {
}
