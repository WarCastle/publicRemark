package com.castle.publicremark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.castle.publicremark.dto.Result;
import com.castle.publicremark.entity.SeckillVoucher;
import com.castle.publicremark.entity.VoucherOrder;
import com.castle.publicremark.mapper.VoucherOrderMapper;
import com.castle.publicremark.service.ISeckillVoucherService;
import com.castle.publicremark.service.IVoucherOrderService;
import com.castle.publicremark.utils.RedisIdWorker;
import com.castle.publicremark.utils.UserHolder;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;

import static com.castle.publicremark.utils.RedisConstants.SECKILL_ORDER_KEY;
import static com.castle.publicremark.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * @author YuLong
 * Date: 2022/11/16 20:40
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(),
                voucherId.toString(), userId.toString(), SECKILL_STOCK_KEY, SECKILL_ORDER_KEY);
        // 2.判断结果是否为0
        assert result != null;
        int r = result.intValue();
        if (r != 0) {
            // 2.1.不为0，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 2.2.为0，有购买资格，把下单信息保存到阻塞队列
        long orderId = redisIdWorker.nextId("order");
        // TODO 保存阻塞队列

        // 3.返回订单id
        return Result.success(orderId);
    }

    // @Override
    // public Result seckillVoucher(Long voucherId) {
    //     // 1.查询优惠券
    //     SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
    //     // 2.判断秒杀是否开始
    //     if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
    //         return Result.fail("秒杀尚未开始！");
    //     }
    //     // 3.判断秒杀是否结束
    //     if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
    //         return Result.fail("秒杀已经结束！");
    //     }
    //     // 4.判断库存是否充足
    //     if (voucher.getStock() < 1) {
    //         // 库存不足
    //         return Result.fail("库存不足！");
    //     }
    //     Long userId = UserHolder.getUser().getId();
    //     // 创建锁对象
    //     // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
    //     RLock lock = redissonClient.getLock("lock:order:" + userId);
    //     // 获取锁
    //     boolean isLock = lock.tryLock();
    //     // 判断是否获取锁成功
    //     if (!isLock) {
    //         // 获取锁失败，返回错误
    //         return Result.fail("不允许重复下单！");
    //     }
    //     try {
    //         // 获取代理对象（事务）
    //         IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
    //         return proxy.createVoucherOrder(voucherId, userId);
    //     } finally {
    //         lock.unlock();
    //     }
    // }

    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrder(Long voucherId, Long userId) {
        // 5.一人一单
        // 5.1.查询订单
        Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        // 5.2.判断是否存在
        if (count > 0) {
            // 用户已经购买过了
            return Result.fail("用户已经购买过一次了！");
        }
        // 6.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!success) {
            return Result.fail("库存不足！");
        }
        // 7.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 7.1.订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 7.2.用户id
        voucherOrder.setUserId(userId);
        // 7.3.代金券id
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        // 8.返回订单id
        return Result.success(orderId);
    }
}
