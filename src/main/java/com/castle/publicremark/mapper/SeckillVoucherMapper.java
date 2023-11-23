package com.castle.publicremark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.castle.publicremark.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author YuLong
 * Date: 2022/11/16 19:50
 *
 * <p>
 *     秒杀优惠券表，与优惠券是一对一关系 Mapper接口
 * </p>
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {
}
