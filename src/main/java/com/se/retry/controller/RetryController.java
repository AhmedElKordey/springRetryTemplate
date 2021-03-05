package com.se.retry.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.se.retry.service.BusinessService;

import lombok.extern.java.Log;

@RestController
@Log
public class RetryController {

	@Autowired
	BusinessService service;

	@GetMapping("/retry")
	public String retry() {
		Gson gson = new Gson();
		List<String> response = new ArrayList<>();
		long start = System.currentTimeMillis();
		
		try {
			CompletableFuture<String> page1 = service.callParallelServices(1);
			CompletableFuture<String> page2 = service.callParallelServices(2);
			CompletableFuture<String> page3 = service.callParallelServices(3);
			CompletableFuture<String> page4 = service.callParallelServices(4);
			CompletableFuture<String> page5 = service.callParallelServices(5);
			CompletableFuture<String> page6 = service.callParallelServices(6);
			CompletableFuture<String> page7 = service.callParallelServices(7);
			CompletableFuture<String> page8 = service.callParallelServices(8);
			CompletableFuture<String> page9 = service.callParallelServices(9);

			// Wait until they are all done
			CompletableFuture.allOf(page1, page2, page3, page4, page5, page6, page7, page8, page9).join();

			response.add(page1.get());
			response.add(page2.get());
			response.add(page3.get());
			response.add(page4.get());
			response.add(page5.get());
			response.add(page6.get());
			response.add(page7.get());
			response.add(page8.get());
			response.add(page9.get());
			
			log.info("Elapsed controller time: " + (System.currentTimeMillis() - start));
		} catch (Exception e) {
			e.printStackTrace();
			response.add("Exp happened");
		}

		return gson.toJson(response);

	}

}
