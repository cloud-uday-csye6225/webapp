package com.neu.cloud.cloudapp.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.neu.cloud.cloudapp.model.User;
import com.neu.cloud.cloudapp.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthHandler {

	@Autowired
	private UserRepository userRepository;

	public User getUser(HttpServletRequest httpRequest) {
		try {
			final String authorization = httpRequest.getHeader("Authorization");
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
				// Authorization: Basic base64credentials
				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				// credentials = username:password
				final String[] values = credentials.split(":", 2);

				User user = userRepository.findByUsername(values[0]);
				if (user != null) {
					if (verifyHash(values[1], user.getPassword())) {
						return user;
					}
				}

//				Optional<User> userOptional = userRepository.findByUsername(values[0]);
//				if (userOptional.isPresent()) {
//					if (verifyHash(values[1], userOptional.get().getPassword())) {
//						return userOptional.get();
//					}
//				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public String hash(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}

	public boolean verifyHash(String password, String hash) {
		return BCrypt.checkpw(password, hash);
	}

}
