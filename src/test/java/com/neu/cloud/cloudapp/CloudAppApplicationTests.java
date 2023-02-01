package com.neu.cloud.cloudapp;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.neu.cloud.cloudapp.Utils.AuthHandler;
import com.neu.cloud.cloudapp.model.User;
import com.neu.cloud.cloudapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootTest
@WebAppConfiguration
@ContextConfiguration
class CloudAppApplicationTests {

	@Autowired
	private UserService userService;

	@Test
	void contextLoads() {
		HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
		AuthHandler authHandler = mock(AuthHandler.class);
		User mockUser = new User();
		mockUser.setFirstName("Jane");
		mockUser.setLastName("Doe");
		mockUser.setUsername("jane.doe@example.com");
		mockUser.setPassword(BCrypt.hashpw("somepassword", BCrypt.gensalt()));
		doReturn(mockUser).when(authHandler).getUser(mockedRequest);
		assertNotEquals(HttpStatusCode.valueOf(200), userService.fetchById("1", mockedRequest).getStatusCode());

	}

}
