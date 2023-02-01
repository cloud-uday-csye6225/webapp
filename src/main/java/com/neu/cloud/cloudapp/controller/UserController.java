package com.neu.cloud.cloudapp.controller;

import java.util.HashMap;
import java.util.Map;

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

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/v1/user")
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody(required = false) Map<String, String> requMap) {
		try {
			return userService.save(requMap);
		} catch (Exception e) {
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid input");
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/v1/user/{userId}")
	public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId,
			HttpServletRequest httpServletRequest) {
		try {
			return userService.findById(userId, httpServletRequest);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@PutMapping("/v1/user/{userId}")
	public ResponseEntity<Map<String, Object>> updateUserById(@PathVariable String userId,
			@RequestBody Map<String, String> requMap, HttpServletRequest httpServletRequest) {
		try {
			return userService.updateUserById(userId, requMap, httpServletRequest);
		} catch (Exception e) {
			Map<String, Object> resMap = new HashMap<>();
			resMap.put("msg", "Please enter valid data");
			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
		}
	}

	@GetMapping("/healthz")
	public ResponseEntity<Object> checkHealth(@RequestBody Map<String, String> requMap) {
		return new ResponseEntity<>(HttpStatusCode.valueOf(200));
	}

}
