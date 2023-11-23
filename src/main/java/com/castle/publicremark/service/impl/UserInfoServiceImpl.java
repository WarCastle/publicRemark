package com.castle.publicremark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.entity.UserInfo;
import com.castle.publicremark.mapper.UserInfoMapper;
import com.castle.publicremark.service.IUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author YuLong
 * Date: 2022/11/16 20:38
 */
@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
}
