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
  @Autowired private StringRedisTemplate redisTemplate;

  @Autowired private CacheRepository cacheRepository;

  @AfterEach
  public void afterEach() {
    Set<String> keys = redisTemplate.keys("*");
    Iterator<String> iterator = keys.iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      redisTemplate.delete(key);
    }
  }

  @Test
  @DisplayName("CacheRepository.saveCache()/getCache(): 정상흐름")
  public void saveCache_ok() {
    // given
    String givenKey = "testKey";
    String givenValue = "testValue";

    // when
    cacheRepository.saveCache(givenKey, givenValue, 120L);

    // then
    String savedValue = cacheRepository.getCache(givenKey).get();
    assertEquals(givenValue, savedValue);
  }

  @Test
  @DisplayName("CacheRepository.getCache(): 캐시가 존재하지 않을 경우")
  public void getCache_NoData() {
    // given
    String wrongKey = "wrongKey";

    // when
    Optional<String> queryResult = cacheRepository.getCache(wrongKey);

    // then
    assertTrue(queryResult.isEmpty());
  }

  @Test
  @DisplayName("CacheRepository.removeCache: 정상흐름")
  public void removeCache_ok() {
    // given
    String givenKey = "testKey";
    String givenValue = "testValue";
    cacheRepository.saveCache(givenKey, givenValue, 120L);

    // then
    cacheRepository.removeCache(givenKey);

    // then
    Optional<String> queryResult = cacheRepository.getCache(givenKey);
    assertTrue(queryResult.isEmpty());
  }

  @Test
  @DisplayName("CacheRepository.hasKey: 정상흐름")
  public void hasKey() {
    // given
    String givenKey = "testKey";
    String givenValue = "testValue";
    String wrongKey = "wrongKey";
    cacheRepository.saveCache(givenKey, givenValue, 120L);

    // when
    assertTrue(cacheRepository.hasKey(givenKey));
    assertFalse(cacheRepository.hasKey(wrongKey));
  }
}
