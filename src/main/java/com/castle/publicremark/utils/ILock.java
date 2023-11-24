package com.castle.publicremark.utils;

/**
 * @author YuLong
 * @Date 2023/11/23 18:02
 * @Classname ILock
 * @Description Lock接口
 */
public interface ILock {

    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true代表获取锁成功; false代表获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unLock();
}
