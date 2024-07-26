package com.project.shoppingmall.repository;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CacheRepository {
  private final StringRedisTemplate redisTemplate;

  public void saveCache(String key, String value, Long expirationTime) {
    redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(expirationTime));
  }

  public Optional<String> getCache(String key) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }

  public void removeCache(String key) {
    redisTemplate.delete(key);
  }

  public boolean hasKey(String key) {
    Boolean keyExists = redisTemplate.hasKey(key);
    return keyExists != null && keyExists;
  }
}
