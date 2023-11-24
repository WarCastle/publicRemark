package com.castle.publicremark.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author YuLong
 * @Date 2023/11/24 13:53
 * @Classname RedissonConfig
 * @Description Redisson配置
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://124.222.111.81:369").setPassword("369yun6379");
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}
