package com.se.retry.service;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import lombok.extern.java.Log;

@Service
@Log
public class BusinessService {

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RetryTemplate retryTemplate;
	
	private static final Gson GSON = new Gson();

	@Async
	public CompletableFuture<String> callParallelServices(int id) {
		return retryTemplate.execute(context -> {
			log.info("call service for id : " + id);
			return getProductList(id);
		}, context -> {
			log.info("failed call for id : " + id);
			return CompletableFuture.completedFuture(GSON.toJson("Error in calling service"));
		});
	}

	private CompletableFuture<String> getProductList(int id) {
		if (id == 3 || id == 7)
			throw new RuntimeException("Custom Exp");

		long start = System.currentTimeMillis();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<>(headers);

		StringBuilder url = new StringBuilder();
		url.append("https://jsonplaceholder.typicode.com/todos/");
		url.append(id);

		String response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, String.class).getBody();

		log.info("Elapsed service time for id : " + id + " is :" + (System.currentTimeMillis() - start));

		return CompletableFuture.completedFuture(GSON.toJson(response));

	}
}
