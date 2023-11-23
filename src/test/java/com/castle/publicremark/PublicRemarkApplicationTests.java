package com.castle.publicremark;

import com.castle.publicremark.entity.Shop;
import com.castle.publicremark.service.impl.ShopServiceImpl;
import com.castle.publicremark.utils.CacheClient;
import com.castle.publicremark.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static com.castle.publicremark.utils.RedisConstants.*;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class PublicRemarkApplicationTests {
    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    private final ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin) + "ms");
    }

    @Test
    void testSaveShop() {
        Long id = 1L;
        Shop shop = shopService.getById(id);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + id, shop, CACHE_SHOP_TTL, TimeUnit.SECONDS);
    }

}
