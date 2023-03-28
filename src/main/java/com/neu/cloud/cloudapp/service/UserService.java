package com.neu.cloud.cloudapp.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.neu.cloud.cloudapp.Utils.AuthHandler;
import com.neu.cloud.cloudapp.Utils.Utils;
import com.neu.cloud.cloudapp.model.User;
import com.neu.cloud.cloudapp.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserService {

	Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthHandler authHandler;

	public ResponseEntity<Map<String, Object>> save(Map<String, String> requMap) {

		Map<String, Object> resMap = new HashMap<>();

		if (requMap.containsKey("first_name") == false || requMap.containsKey("last_name") == false
				|| requMap.containsKey("password") == false || requMap.containsKey("username") == false) {
			resMap.put("msg", "Please enter all valid input fields");
			logger.error("user creation failed as it does not have all required fields");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
		String firstName = requMap.get("first_name");
		String lastName = requMap.get("last_name");
		String username = requMap.get("username");
		String password = requMap.get("password");

		if (Utils.isOnlyText(firstName) == false || Utils.isOnlyText(lastName) == false
				|| Utils.isValidString(password) == false || Utils.isEmailValidated(username) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter valid input");
			logger.error("user creation failed as it does not have all valid values to fields");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		User userExists = userRepository.findByUsername(username);
		if (userExists != null) {
			resMap.clear();
			resMap.put("msg", "Email already exists");
			logger.error("user creation failed as email is not unique");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		User user = new User();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setPassword(authHandler.hash(password));
		user.setAccountCreated(LocalDateTime.now().toString());
		user.setAccountUpdated(LocalDateTime.now().toString());
		userRepository.save(user);
		logger.info("user creation done with id " + user.getId());
		resMap.clear();
		resMap.put("id", user.getId());
		resMap.put("first_name", user.getFirstName());
		resMap.put("last_name", user.getLastName());
		resMap.put("username", user.getUsername());
		resMap.put("account_created", user.getAccountCreated());
		resMap.put("account_updated", user.getAccountUpdated());

		return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(201));
	}

	public ResponseEntity<Map<String, Object>> fetchById(String userId, HttpServletRequest httpServletRequest) {
		Map<String, Object> resMap = new HashMap<>();
		if (Utils.isValidString(userId) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter input id");
			logger.error("user fetch failed as userID is not valid " + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (Utils.isOnlyNumber(userId.trim()) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter valid id integer");
			logger.error("user fetch failed as userID is not valid integer " + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
		int givenUserId = Integer.parseInt(userId);

		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			resMap.clear();
			resMap.put("msg", "Please enter valid credentials");
			logger.error("user fetch failed, credentials do not match " + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (givenUserId != authUser.getId()) {
			resMap.clear();
			resMap.put("msg", "Forbidden to view the data");
			logger.error("user fetch failed as it's forbidden " + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		resMap.clear();
		User user = authUser;
		resMap.put("id", user.getId());
		resMap.put("first_name", user.getFirstName());
		resMap.put("last_name", user.getLastName());
		resMap.put("username", user.getUsername());
		resMap.put("account_created", user.getAccountCreated());
		resMap.put("account_updated", user.getAccountUpdated());
		logger.info("user data fetched successfully with " + userId);
		return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(200));
	}

	public ResponseEntity<Map<String, Object>> updateUserById(String userId, Map<String, String> requMap,
			HttpServletRequest httpServletRequest) {
		Map<String, Object> resMap = new HashMap<>();
		HashSet<String> set = new HashSet<>();
		set.add("first_name");
		set.add("last_name");
		set.add("password");
		int c = 0;

		for (String str : requMap.keySet()) {
			if (set.contains(str)) {
				c++;
			}
		}
		if (c == 0) {
			resMap.clear();
			resMap.put("msg", "No fields to update");
			logger.warn("No given fields are eligible to update");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (Utils.isValidString(userId) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter input id");
			logger.error("Given userId field is not valid string" + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (Utils.isOnlyNumber(userId.trim()) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter valid id integer");
			logger.error("Given userId field is not valid integer" + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
		int givenUserId = Integer.parseInt(userId);

		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			resMap.clear();
			resMap.put("msg", "Please enter valid credentials");
			logger.error("user fetch failed, credentials do not match " + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (givenUserId != authUser.getId()) {
			resMap.clear();
			resMap.put("msg", "Forbidden to view the data");
			logger.error("user update failed as it's forbidden " + userId);
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		User user = authUser;

		String firstName = requMap.getOrDefault("first_name", null);
		String lastName = requMap.getOrDefault("last_name", null);
		String password = requMap.getOrDefault("password", null);

		if (Utils.isOnlyText(firstName)) {
			user.setFirstName(firstName);
		}

		if (Utils.isOnlyText(lastName)) {
			user.setLastName(lastName);
		}

		if (Utils.isValidString(password)) {
			user.setPassword(authHandler.hash(password));
		}

		user.setAccountUpdated(LocalDateTime.now().toString());
		userRepository.save(user);
		logger.info("user data updated successfully with " + userId);
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));
	}

}
