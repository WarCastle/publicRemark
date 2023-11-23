package com.castle.publicremark.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author YuLong
 * Date: 2022/11/16 19:36
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
