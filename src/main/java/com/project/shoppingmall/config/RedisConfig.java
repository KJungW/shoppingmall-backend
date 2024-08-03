package com.project.shoppingmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.mail.host}")
  private String host;

  @Value("${spring.data.redis.mail.port}")
  private int port;

  public RedisConnectionFactory redisMailConnectionFactory() {
    LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(host, port);
    connectionFactory.start();
    return connectionFactory;
  }

  @Bean
  public StringRedisTemplate redisTemplate() {
    StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
    stringRedisTemplate.setConnectionFactory(redisMailConnectionFactory());
    return stringRedisTemplate;
  }
}
