package com.castle.publicremark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.Voucher;
import com.castle.publicremark.entity.VoucherOrder;

/**
 * @author YuLong
 * Date: 2022/11/16 20:05
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId, Long userId);
}
