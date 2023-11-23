package com.castle.publicremark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.Voucher;

/**
 * @author YuLong
 * Date: 2022/11/16 20:58
 */
public interface IVoucherService extends IService<Voucher> {

    /**
     * 查找商店代金券
     * @param shopId
     * @return
     */
    Result queryVoucherOfShop(Long shopId);

    /**
     * 添加秒杀代金券
     * @param voucher
     */
    void addSecKillVoucher(Voucher voucher);
}
