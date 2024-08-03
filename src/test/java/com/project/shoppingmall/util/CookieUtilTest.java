package com.project.shoppingmall.util;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

class CookieUtilTest {
  private CookieUtil target;

  @BeforeEach
  public void beforeEach() {
    target = new CookieUtil();
    ReflectionTestUtils.setField(target, "deployEnv", "tempEnv");
    ReflectionTestUtils.setField(target, "domain", "tempDomain");
  }

  @Test
  @DisplayName("createCookie() : dev 환경")
  public void createCookie_inDev() {
    // given
    // - 환경 세팅
    String givenDeployEnv = "dev";
    String givenDomain = "devDomain";
    ReflectionTestUtils.setField(target, "deployEnv", givenDeployEnv);
    ReflectionTestUtils.setField(target, "domain", givenDomain);

    // - 인자 세팅
    String rightKey = "testKey";
    String rightValue = "testValue";
    int rightMaxAge = 120;

    // when
    ResponseCookie resultCookie = target.createCookie(rightKey, rightValue, rightMaxAge);

    // then
    assertEquals(rightKey, resultCookie.getName());
    assertEquals(rightValue, resultCookie.getValue());
    assertEquals(givenDomain, resultCookie.getDomain());
    assertEquals("/", resultCookie.getPath());
    assertEquals("strict", resultCookie.getSameSite());
    assertTrue(resultCookie.isHttpOnly());
    assertFalse(resultCookie.isSecure());
    assertEquals(Duration.ofSeconds(rightMaxAge), resultCookie.getMaxAge());
  }

  @Test
  @DisplayName("createCookie() : stage 환경")
  public void createCookie_inStage() {
    // given
    // - 환경 세팅
    String givenDeployEnv = "stage";
    String givenDomain = "stageDomain";
    ReflectionTestUtils.setField(target, "deployEnv", givenDeployEnv);
    ReflectionTestUtils.setField(target, "domain", givenDomain);

    // - 인자 세팅
    String rightKey = "testKey";
    String rightValue = "testValue";
    int rightMaxAge = 120;

    // when
    ResponseCookie resultCookie = target.createCookie(rightKey, rightValue, rightMaxAge);

    // then
    assertEquals(rightKey, resultCookie.getName());
    assertEquals(rightValue, resultCookie.getValue());
    assertEquals(givenDomain, resultCookie.getDomain());
    assertEquals("/", resultCookie.getPath());
    assertEquals("strict", resultCookie.getSameSite());
    assertTrue(resultCookie.isHttpOnly());
    assertTrue(resultCookie.isSecure());
    assertEquals(Duration.ofSeconds(rightMaxAge), resultCookie.getMaxAge());
  }

  @Test
  @DisplayName("createCookie() : prod 환경")
  public void createCookie_inProd() {
    // given
    // - 환경 세팅
    String givenDeployEnv = "prod";
    String givenDomain = "prodDomain";
    ReflectionTestUtils.setField(target, "deployEnv", givenDeployEnv);
    ReflectionTestUtils.setField(target, "domain", givenDomain);

    // - 인자 세팅
    String rightKey = "testKey";
    String rightValue = "testValue";
    int rightMaxAge = 120;

    // when
    ResponseCookie resultCookie = target.createCookie(rightKey, rightValue, rightMaxAge);

    // then
    assertEquals(rightKey, resultCookie.getName());
    assertEquals(rightValue, resultCookie.getValue());
    assertEquals(givenDomain, resultCookie.getDomain());
    assertEquals("/", resultCookie.getPath());
    assertEquals("strict", resultCookie.getSameSite());
    assertTrue(resultCookie.isHttpOnly());
    assertTrue(resultCookie.isSecure());
    assertEquals(Duration.ofSeconds(rightMaxAge), resultCookie.getMaxAge());
  }

  @Test
  @DisplayName("createCookie() : test 환경")
  public void createCookie_inTest() {
    // given
    // - 환경 세팅
    String givenDeployEnv = "test";
    String givenDomain = "testDomain";
    ReflectionTestUtils.setField(target, "deployEnv", givenDeployEnv);
    ReflectionTestUtils.setField(target, "domain", givenDomain);

    // - 인자 세팅
    String rightKey = "testKey";
    String rightValue = "testValue";
    int rightMaxAge = 120;

    // when
    ResponseCookie resultCookie = target.createCookie(rightKey, rightValue, rightMaxAge);

    // then
    assertEquals(rightKey, resultCookie.getName());
    assertEquals(rightValue, resultCookie.getValue());
    assertEquals(givenDomain, resultCookie.getDomain());
    assertEquals("/", resultCookie.getPath());
    assertEquals("strict", resultCookie.getSameSite());
    assertTrue(resultCookie.isHttpOnly());
    assertTrue(resultCookie.isSecure());
    assertEquals(Duration.ofSeconds(rightMaxAge), resultCookie.getMaxAge());
  }

  @Test
  @DisplayName("findCookie() : 정상흐름")
  public void findCookie_ok() {
    // given
    // - key 인자 세팅
    String rightKey = "rigthKey";
    // - cookies 인자 세팅
    String rightValue = "rightValue";
    Cookie rightCookie = new Cookie(rightKey, rightValue);
    Cookie[] rightCookies = {
      new Cookie("testKey1", "testValue1"),
      new Cookie("testKey2", "testValue2"),
      rightCookie,
      new Cookie("testKey3", "testValue3"),
      new Cookie("testKey4", "testValue4"),
    };

    // when
    String result = target.findCookie(rightKey, rightCookies);

    // then
    assertEquals(rightValue, result);
  }

  @Test
  @DisplayName("findCookie() : 해당하는 쿠키가 존재하지 않음")
  public void findCookie_noCookie() {
    // given
    // - key 인자 세팅
    String rightKey = "rigthKey";
    // - cookies 인자 세팅
    Cookie[] wrongCookie = {
      new Cookie("testKey1", "testValue1"),
      new Cookie("testKey2", "testValue2"),
      new Cookie("testKey3", "testValue3"),
      new Cookie("testKey4", "testValue4"),
    };

    // when
    String result = target.findCookie(rightKey, wrongCookie);

    // then
    assertEquals("", result);
  }
}
