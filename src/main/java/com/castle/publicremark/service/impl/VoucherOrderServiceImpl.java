package com.castle.publicremark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.entity.VoucherOrder;
import com.castle.publicremark.mapper.VoucherOrderMapper;
import com.castle.publicremark.service.IVoucherOrderService;
import org.springframework.stereotype.Service;

/**
 * @author YuLong
 * Date: 2022/11/16 20:40
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
}
