package com.stephen.answer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @author stephen qiu
 */
@SpringBootApplication
@MapperScan("com.stephen.answer.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class AnswerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(AnswerApplication.class, args);
	}
	
}