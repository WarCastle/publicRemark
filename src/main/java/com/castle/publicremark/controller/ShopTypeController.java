package com.castle.publicremark.controller;

import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.ShopType;
import com.castle.publicremark.service.IShopService;
import com.castle.publicremark.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YuLong
 * Date: 2022/11/17 11:43
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}
