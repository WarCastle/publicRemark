package com.castle.publicremark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.castle.publicremark.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author YuLong
 * Date: 2022/11/16 19:53
 */
@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {
    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
