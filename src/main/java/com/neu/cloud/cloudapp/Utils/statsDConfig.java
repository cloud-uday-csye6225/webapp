package com.neu.cloud.cloudapp.Utils;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class statsDConfig {
	@Bean
	public StatsDClient statsDClient(
			@Value("${metrics.statsd.host:localhost}") String host,
			@Value("${metrics.statsd.port:8125}") int port, 
			@Value("${metrics.prefix:webapp}") String prefix) {
		return new NonBlockingStatsDClient(prefix, host, port);
	}
}
