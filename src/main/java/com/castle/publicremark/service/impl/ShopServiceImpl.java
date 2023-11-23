package com.castle.publicremark.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.Shop;
import com.castle.publicremark.mapper.ShopMapper;
import com.castle.publicremark.service.IShopService;
import com.castle.publicremark.utils.CacheClient;
import com.castle.publicremark.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.castle.publicremark.utils.RedisConstants.*;

/**
 * @author YuLong
 * Date: 2022/11/16 20:33
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    /**
     * 根据id查询商铺信息
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) {

        // // 互斥锁解决缓存击穿
        // Shop shopInfo = queryWithMutex(id);

        // 逻辑过期解决缓存击穿
        // Shop shopInfo = queryWithLogicalExpire(id);


        // 利用缓存封装工具和逻辑过期解决缓存穿透
        Long cacheShopTime = CACHE_SHOP_TTL + RandomUtil.randomLong(CACHE_SHOP_TTL);
        Shop shopInfo = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class,
                this::getById, cacheShopTime, TimeUnit.SECONDS);
        if (shopInfo == null) {
            return Result.fail("店铺不存在");
        }
        // 返回商铺信息
        return Result.success(shopInfo);
    }

    /**
     * 更新商铺信息
     * @param shop
     * @return
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.success();
    }

}
