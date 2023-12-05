package com.castle.publicremark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.castle.publicremark.dto.LoginFormDTO;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.User;

import javax.servlet.http.HttpSession;

/**
 * @author YuLong
 * Date: 2022/11/16 20:03
 */
public interface IUserService extends IService<User> {
    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 用户登录
     * @param loginForm
     * @param session
     * @return
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
