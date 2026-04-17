package com.lowcarbon.platform.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.lowcarbon.platform.mapper")
public class MybatisPlusConfig {
}

