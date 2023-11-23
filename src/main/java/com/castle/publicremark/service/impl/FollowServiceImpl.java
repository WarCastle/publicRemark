package com.castle.publicremark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.entity.Follow;
import com.castle.publicremark.mapper.FollowMapper;
import com.castle.publicremark.service.IFollowService;
import org.springframework.stereotype.Service;

/**
 * @author YuLong
 * Date: 2022/11/16 20:24
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
}
