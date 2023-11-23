package com.castle.publicremark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.castle.publicremark.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author YuLong
 * Date: 2022/11/16 19:53
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
