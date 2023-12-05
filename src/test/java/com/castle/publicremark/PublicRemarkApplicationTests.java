package com.castle.publicremark;

import com.castle.publicremark.entity.Shop;
import com.castle.publicremark.service.impl.ShopServiceImpl;
import com.castle.publicremark.utils.CacheClient;
import com.castle.publicremark.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.castle.publicremark.utils.RedisConstants.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
class PublicRemarkApplicationTests {
    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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

    @Test
    void loadShopData() {
        // 1.查询店铺信息
        List<Shop> list = shopService.list();
        // 2.把店铺分组，按照typeId分组，typeId一致的放到一个集合中
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 3.分批写入Redis
        map.entrySet().forEach(entry -> {
            // 3.1.获取类型id
            Long typeId = entry.getKey();
            String geoKey = SHOP_GEO_KEY + typeId;
            // 3.2.获取同类型的店铺集合
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            // 3.3.写入Redis GEOADD key 经度 纬度 member
            value.forEach(shop -> {
                // 第一种方案，每一个店铺提交一次（单个写）
                // stringRedisTemplate.opsForGeo().add(geoKey, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                // 第二种方案，同一类型店铺添加到集合中再提交（批量写）
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(), new Point(shop.getX(), shop.getY())));
            });
            stringRedisTemplate.opsForGeo().add(geoKey, locations);
        });
    }
}
