package com.castle.publicremark.controller;

import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.Voucher;
import com.castle.publicremark.service.IVoucherOrderService;
import com.castle.publicremark.service.IVoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author YuLong
 * Date: 2022/11/17 11:44
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;


    /**
     * 新增普通券
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.success(voucher.getId());
    }

    /**
     * 新增秒杀券
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSecKillVoucher(voucher);
        return Result.success(voucher.getId());
    }

    /**
     * 查询店铺的优惠券列表
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }

}
