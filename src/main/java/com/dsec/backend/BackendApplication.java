package com.dsec.backend;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {
	  final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	  executor.setCorePoolSize(2);
	  executor.setMaxPoolSize(2);
	  executor.setQueueCapacity(100);
	  executor.setThreadNamePrefix("Thread-");
	  executor.initialize();
	  return executor;
	}

}
