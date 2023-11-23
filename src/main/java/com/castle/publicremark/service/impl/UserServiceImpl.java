package com.castle.publicremark.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.dto.LoginFormDTO;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.dto.UserDTO;
import com.castle.publicremark.entity.User;
import com.castle.publicremark.mapper.UserMapper;
import com.castle.publicremark.service.IUserService;
import com.castle.publicremark.utils.RegexUtils;
import com.castle.publicremark.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.castle.publicremark.utils.RedisConstants.*;
import static com.castle.publicremark.utils.SystemConstants.AUTH_PHONE;
import static com.castle.publicremark.utils.SystemConstants.USER_NICK_AFTER_DIGIT;

/**
 * @author YuLong
 * Date: 2022/11/16 20:39
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到 redis
        long authCodeTime = LOGIN_CODE_TTL + RandomUtil.randomLong(LOGIN_CODE_TTL);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, authCodeTime, TimeUnit.MINUTES);
        log.info("点击发送验证码时获取到的手机号码： {}", phone);
        // 5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 返回success
        return Result.success();
    }

    /**
     * 用户登录
     * @param loginForm
     * @param session
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        log.info("点击登录后获取到的手机号码 = {}", phone);
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        // 2.从redis中获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 3.不一致，报错
            return Result.fail("验证码错误");
        }

        // 4.一致，根据手机号查询用户 select * from tb_user where phone = ?
        User user = query().eq(AUTH_PHONE, phone).one();

        // 5.判断用户是否存在
        if (user == null) {
            // 6.用户不存在，创建新用户并保存到数据库
            log.info("用户不存在，创建新用户中。。。");
            user = createUserWithPhone(phone);
        }
        // 7.保存用户信息到 redis
        // 7.1.随机生成 token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2.将 User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor( (fieldName, fieldValue) -> fieldValue.toString())
        );
        // 7.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        log.info("tokenKey = {}", stringRedisTemplate.opsForHash().entries(tokenKey));
        // 7.4.设置 token有效期
        long loginTime = LOGIN_USER_TTL + RandomUtil.randomLong(LOGIN_USER_TTL) / 2;
        stringRedisTemplate.expire(tokenKey, loginTime, TimeUnit.SECONDS);
        log.info("用户登录成功");

        // 8.返回 token
        return Result.success(token);
    }

    private User createUserWithPhone(String phone) {
        String username = SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(USER_NICK_AFTER_DIGIT);
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(username);

        // 2.保存用户
        save(user);
        log.info("创建新用户成功，新用户昵称为：{}", username);
        return user;
    }

}
