package com.castle.publicremark.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.ShopType;
import com.castle.publicremark.mapper.ShopTypeMapper;
import com.castle.publicremark.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import static com.castle.publicremark.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * @author YuLong
 * Date: 2022/11/16 20:35
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        // 先查缓存redis是否有数据
        String value = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        // 缓存有则直接返回
        if (value != null) {
            return Result.success(JSONUtil.toList(value, ShopType.class));
        }
        // 如果缓存没有则查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();

        // 将数据保存到缓存中
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopTypeList));

        // 响应数据
        return Result.success(shopTypeList);
    }
}
