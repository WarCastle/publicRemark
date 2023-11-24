package com.castle.publicremark;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author YuLong
 * @Date 2023/11/24 16:43
 * @Classname RedissonTest
 * @Description Redisson测试类
 */
@Slf4j
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedissonClient redissonClient2;
    @Resource
    private RedissonClient redissonClient3;
    private RLock lock;
    
    @BeforeEach
    void setUp() {
        RLock lock1 = redissonClient.getLock("order");
        RLock lock2 = redissonClient2.getLock("order");
        RLock lock3 = redissonClient3.getLock("order");

        // 创建联锁 multiLock
        lock = redissonClient.getMultiLock(lock1, lock2, lock3);
    }

    @Test
    void method01() throws InterruptedException {
        // 尝试获取锁
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.info("获取锁失败");
            return;
        }
        try {
            log.info("获取锁成功 .... 1");
            method02();
            log.info("开始执行业务 .... 1");
        } finally {
            log.warn("准备释放锁 .... 1");
            lock.unlock();
        }
    }

    void method02() throws InterruptedException {
        // 尝试获取锁
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.info("获取锁失败");
            return;
        }
        try {
            log.info("获取锁成功 .... 2");
            log.info("开始执行业务 ....2");
        } finally {
            log.warn("准备释放锁 .... 2");
            lock.unlock();
        }
    }
}
