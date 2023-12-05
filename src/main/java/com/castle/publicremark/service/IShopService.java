package com.castle.publicremark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.Shop;

/**
 * @author YuLong
 * Date: 2022/11/16 20:02
 */
public interface IShopService extends IService<Shop> {
    /**
     * 根据id查询商铺信息
     * @param id
     * @return
     */
    Result queryById(Long id);


    /**
     * 更新商铺信息
     * @param shop
     * @return
     */
    Result update(Shop shop);

    /**
     * 根据类型查询商铺
     * @param typeId
     * @param current
     * @param x
     * @param y
     * @return
     */
    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
