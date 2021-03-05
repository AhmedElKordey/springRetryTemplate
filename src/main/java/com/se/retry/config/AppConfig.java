package com.se.retry.config;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import lombok.extern.java.Log;

@Configuration
@EnableRetry
@EnableAsync
@Log
public class AppConfig implements AsyncConfigurer{

	@Value( "${maxAttempts}" )
	private int maxAttempts ;
	@Value( "${maxDelay}" )
	private long maxDelay;
	@Value( "${restTimeOut}" )
	private long restTimeOut;
	@Value( "${threadPoolSize}" )
	private int threadPoolSize; 
	@Value( "${maxThreadPoolSize}" )
	private int maxThreadPoolSize;
	@Value( "${queueCapacity}" )
	private int queueCapacity;
	@Value( "${threadNamePrefix}" )
	private String threadNamePrefix;
	
	@Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
		
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(maxDelay); // BackOffPolicy is used to control backoff between retry attempts
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(); // RetryPolicy determines when an operation should be retried
        retryPolicy.setMaxAttempts(maxAttempts); // A SimpleRetryPolicy is used to retry a fixed number of times
        retryTemplate.setRetryPolicy(retryPolicy); // FixedBackOffPolicy pauses for a fixed period of time before continuing
        retryTemplate.registerListener(new DefaultListenerSupport());
      
        return retryTemplate;
    }
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.setConnectTimeout(Duration.ofMillis(restTimeOut)).setReadTimeout(Duration.ofMillis(restTimeOut)).build();
	}

	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(threadPoolSize);
		executor.setMaxPoolSize(maxThreadPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix(threadNamePrefix);
		executor.initialize();
		return executor;
	}
	
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AsyncUncaughtExceptionHandler() {
			@Override
			public void handleUncaughtException(Throwable ex, Method method, Object... params) {
				log.info("Throwable Exception message : " + ex.getMessage());
				log.info("Method name                 : " + method.getName());
				for (Object param : params) {
					log.info("Parameter value             : " + param);
				}
				log.info("stack Trace ");
				ex.printStackTrace();
			}
		};
	}
	
}
