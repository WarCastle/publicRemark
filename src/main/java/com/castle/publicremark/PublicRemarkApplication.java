package com.castle.publicremark;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author YuLong
 * Date: 2022/11/16 17:28
 */
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.castle.publicremark.mapper")
@SpringBootApplication
public class PublicRemarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublicRemarkApplication.class, args);
        log.info("太湖点评项目启动成功");
    }

}
