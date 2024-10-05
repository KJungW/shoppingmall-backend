package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class CacheRepositoryTest {
  @Autowired private CacheRepository target;
  @Autowired private StringRedisTemplate redisTemplate;

  @AfterEach
  public void afterEach() {
    resetRedisCache();
  }

  @Test
  @DisplayName("CacheRepository.saveCache()/getCache(): 정상흐름")
  public void saveCache_ok() {
    // given
    String inputKey = "testKey";
    String inputValue = "testValue";

    // when
    target.saveCache(inputKey, inputValue, 120L);

    // then
    String savedValue = target.getCache(inputKey).get();
    assertEquals(inputValue, savedValue);
  }

  @Test
  @DisplayName("CacheRepository.getCache(): 캐시가 존재하지 않을 경우")
  public void getCache_NoData() {
    // given
    String inputKey = "testKey";

    // when
    Optional<String> queryResult = target.getCache(inputKey);

    // then
    assertTrue(queryResult.isEmpty());
  }

  @Test
  @DisplayName("CacheRepository.removeCache: 정상흐름")
  public void removeCache_ok() {
    // given
    String inputKey = "testKey";

    String givenValue = "testValue";
    target.saveCache(inputKey, givenValue, 120L);

    // then
    target.removeCache(inputKey);

    // then
    assertTrue(target.getCache(inputKey).isEmpty());
  }

  @Test
  @DisplayName("CacheRepository.hasKey: 정상흐름")
  public void hasKey() {
    // given
    String inputKey = "testKey";

    String otherKey = "wrongKey";
    String otherValue = "testValue";
    target.saveCache(otherKey, otherValue, 120L);

    // when
    assertFalse(target.hasKey(inputKey));
    assertTrue(target.hasKey(otherKey));
  }

  public void resetRedisCache() {
    Set<String> keys = redisTemplate.keys("*");
    Iterator<String> iterator = keys.iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      redisTemplate.delete(key);
    }
  }
}
