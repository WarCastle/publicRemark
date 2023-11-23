package com.castle.publicremark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.ShopType;

import java.util.List;

/**
 * @author YuLong
 * Date: 2022/11/16 20:02
 */
public interface IShopTypeService extends IService<ShopType> {
    Result queryTypeList();
}
