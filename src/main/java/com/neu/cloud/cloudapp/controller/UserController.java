package com.neu.cloud.cloudapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.neu.cloud.cloudapp.service.UserService;
import com.timgroup.statsd.StatsDClient;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UserController {

	Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private StatsDClient statsDClient;

	private String postCreateUserString = "post.createuser.count";
	private String getUserString = "get.user.count";
	private String putUserString = "put.user.count";
	private String getHealthString = "get.health.count";

	@PostMapping("/v1/user")
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody(required = false) Map<String, String> requMap) {
		try {
			statsDClient.incrementCounter(postCreateUserString);
			logger.info("creating user data");
			return userService.save(requMap);
		} catch (Exception e) {
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid input");
			logger.error("error while creating user data" + e.getMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/v1/user/{userId}")
	public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId,
			HttpServletRequest httpServletRequest) {
		try {
			statsDClient.incrementCounter(getUserString);
			logger.info("fetching user details");
			return userService.fetchById(userId, httpServletRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("error while fetching user data" + e.getMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@PutMapping("/v1/user/{userId}")
	public ResponseEntity<Map<String, Object>> updateUserById(@PathVariable String userId,
			@RequestBody Map<String, String> requMap, HttpServletRequest httpServletRequest) {
		try {
			statsDClient.incrementCounter(putUserString);
			logger.info("updating user details");
			return userService.updateUserById(userId, requMap, httpServletRequest);
		} catch (Exception e) {
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			logger.error("error while updating user data" + e.getMessage());
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/healthz")
	public ResponseEntity<Object> checkHealth() {
		statsDClient.incrementCounter(getHealthString);
		logger.info("ping for app health check");
		return new ResponseEntity<>(HttpStatusCode.valueOf(200));
	}

}
