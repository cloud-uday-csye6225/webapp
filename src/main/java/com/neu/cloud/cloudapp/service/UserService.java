package com.neu.cloud.cloudapp.service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

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
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthHandler authHandler;

	public ResponseEntity<Map<String, Object>> save(Map<String, String> requMap) {

		Map<String, Object> resMap = new HashMap<>();

		if (requMap.containsKey("first_name") == false || requMap.containsKey("last_name") == false
				|| requMap.containsKey("password") == false || requMap.containsKey("username") == false) {
			resMap.put("msg", "Please enter all valid input fields");
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
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		Optional<User> userExists = userRepository.findByUsername(username);
		if (userExists.isPresent()) {
			resMap.clear();
			resMap.put("msg", "Email already exists");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		User user = new User();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setPassword(authHandler.hash(password));
		user.setAccountCreated(new Date());
		user.setAccountUpdated(new Date());
		userRepository.save(user);

		resMap.clear();
		resMap.put("id", user.getId());
		resMap.put("first_name", user.getFirstName());
		resMap.put("last_name", user.getLastName());
		resMap.put("username", user.getUsername());
		resMap.put("account_created", user.getAccountCreated());
		resMap.put("account_updated", user.getAccountUpdated());

		return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(201));
	}

	public ResponseEntity<Map<String, Object>> findById(String userId, HttpServletRequest httpServletRequest) {
		Map<String, Object> resMap = new HashMap<>();
		if (Utils.isValidString(userId) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter input id");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (Utils.isOnlyNumber(userId.trim()) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter valid id integer");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
		int givenUserId = Integer.parseInt(userId);

		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			resMap.clear();
			resMap.put("msg", "Please enter valid credentials");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (givenUserId != authUser.getId()) {
			resMap.clear();
			resMap.put("msg", "Forbidden to view the data");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

		resMap.clear();
		addDataToResponse(resMap, authUser.getUsername());
		return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(200));
	}

	private void addDataToResponse(Map<String, Object> resMap, String username) {
		Optional<User> userExists = userRepository.findByUsername(username);
		User user = userExists.get();
		resMap.put("id", user.getId());
		resMap.put("first_name", user.getFirstName());
		resMap.put("last_name", user.getLastName());
		resMap.put("username", user.getUsername());
		resMap.put("account_created", user.getAccountCreated());
		resMap.put("account_updated", user.getAccountUpdated());
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
			if (set.contains(str) == false) {
				resMap.clear();
				resMap.put("msg", "Only limited fields are alllowed to update");
				return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
			}
			c++;
		}
		if (c == 0) {
			resMap.clear();
			resMap.put("msg", "No fields to update");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (Utils.isValidString(userId) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter input id");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}

		if (Utils.isOnlyNumber(userId.trim()) == false) {
			resMap.clear();
			resMap.put("msg", "Please enter valid id integer");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(400));
		}
		int givenUserId = Integer.parseInt(userId);

		User authUser = authHandler.getUser(httpServletRequest);
		if (authUser == null) {
			resMap.clear();
			resMap.put("msg", "Please enter valid credentials");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(401));
		}

		if (givenUserId != authUser.getId()) {
			resMap.clear();
			resMap.put("msg", "Forbidden to view the data");
			return new ResponseEntity<>(null, HttpStatusCode.valueOf(403));
		}

//		Optional<User> userExists = userRepository.findById(Integer.parseInt(userId));
//		if (!userExists.isPresent()) {
//			resMap.clear();
//			resMap.put("msg", "No User exists with given id");
//			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(400));
//		}
		User user = authUser;

//		if (user.getId() != authUser.getId()) {
//			resMap.clear();
//			resMap.put("msg", "Forbidden to update the data");
//			return new ResponseEntity<>(resMap, HttpStatusCode.valueOf(403));
//		}

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

		user.setAccountUpdated(new Date());
		userRepository.save(user);
		return new ResponseEntity<>(null, HttpStatusCode.valueOf(204));
	}

}
