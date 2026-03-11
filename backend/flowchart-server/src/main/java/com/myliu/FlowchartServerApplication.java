package com.myliu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyLiu Flowchart Server 主启动类
 *
 * @author MyLiu
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.myliu.mapper")
public class FlowchartServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowchartServerApplication.class, args);
    }
}
