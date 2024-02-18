package com.marcusdacoregio.rinhaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(TestContainersConfig.class)
public class TestRinhaApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(RinhaApiApplication::main).with(TestRinhaApiApplication.class).run(args);
	}

}
