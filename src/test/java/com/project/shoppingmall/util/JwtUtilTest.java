package com.project.shoppingmall.util;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.type.JwtTokenType;
import com.project.shoppingmall.type.MemberRoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {
  private JwtUtil jwtUtil;
  private SecretKey secretKey;
  private Long refreshExpirationTime = 86400000L;
  private Long accessExpirationTime = 600000L;

  @BeforeEach
  public void beforeEach() {
    String secretKeyString = "secreatkey1111cikewcisodkwnvioscnwlql1355123ksdfier";
    jwtUtil = new JwtUtil(secretKeyString);
    ReflectionTestUtils.setField(jwtUtil, "refreshExpirationTime", refreshExpirationTime);
    ReflectionTestUtils.setField(jwtUtil, "accessExpirationTime", accessExpirationTime);
    secretKey = (SecretKey) ReflectionTestUtils.getField(jwtUtil, "secretKey");
  }

  @Test
  @DisplayName("JwtUtil.createRefreshToken() : 정상흐름")
  public void createRefreshToken_ok() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;

    // when
    String resultToken = jwtUtil.createRefreshToken(new RefreshTokenData(givenId, givenRole));

    // then
    Claims payload =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(resultToken).getPayload();
    Long expectedExpireTime = payload.get("iat", Long.class) + refreshExpirationTime / 1000;

    assertEquals(JwtTokenType.REFRESH, JwtTokenType.valueOf(payload.get("type", String.class)));
    assertEquals(givenId, payload.get("id", Long.class));
    assertEquals(givenRole, MemberRoleType.valueOf(payload.get("role", String.class)));
    assertEquals(expectedExpireTime, payload.get("exp", Long.class));
  }

  @Test
  @DisplayName("JwtUtil.createAccessToken() : 정상흐름")
  public void createAccessToken_ok() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;

    // when
    String resultToken = jwtUtil.createAccessToken(new AccessTokenData(givenId, givenRole));

    // then
    Claims payload =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(resultToken).getPayload();
    Long expectedExpireTime = payload.get("iat", Long.class) + accessExpirationTime / 1000;

    assertEquals(JwtTokenType.ACCESS, JwtTokenType.valueOf(payload.get("type", String.class)));
    assertEquals(givenId, payload.get("id", Long.class));
    assertEquals(givenRole, MemberRoleType.valueOf(payload.get("role", String.class)));
    assertEquals(expectedExpireTime, payload.get("exp", Long.class));
  }

  @Test
  @DisplayName("JwtUtil.decodeRefreshToken() : 정상흐름")
  public void decodeRefreshToken_ok() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    String givenToken = jwtUtil.createRefreshToken(new RefreshTokenData(givenId, givenRole));

    // when
    RefreshTokenData resultTokenData = jwtUtil.decodeRefreshToken(givenToken);

    // then
    assertEquals(givenId, resultTokenData.getId());
    assertEquals(givenRole, resultTokenData.getRoleType());
  }

  @Test
  @DisplayName("JwtUtil.decodeRefreshToken() : 파손된 토큰")
  public void decodeRefreshToken_damagedToken() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    String givenToken = jwtUtil.createRefreshToken(new RefreshTokenData(givenId, givenRole));
    String damegedToken = givenToken.substring(1);

    // when then
    assertThrows(
        JwtTokenException.class,
        () -> {
          jwtUtil.decodeRefreshToken(damegedToken);
        });
  }

  @Test
  @DisplayName("JwtUtil.decodeRefreshToken() : 유효기간이 지난 토큰")
  public void decodeRefreshToken_expiredToken() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    ReflectionTestUtils.setField(jwtUtil, "refreshExpirationTime", -100000000L);
    String expiredToken = jwtUtil.createRefreshToken(new RefreshTokenData(givenId, givenRole));

    // when then
    assertThrows(
        JwtTokenException.class,
        () -> {
          jwtUtil.decodeRefreshToken(expiredToken);
        });
  }

  @Test
  @DisplayName("JwtUtil.decodeRefreshToken() : 타입이 다른 토큰")
  public void decodeRefreshToken_incorrectType() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    String accessToken = jwtUtil.createAccessToken(new AccessTokenData(givenId, givenRole));

    // when then
    assertThrows(
        JwtTokenException.class,
        () -> {
          jwtUtil.decodeRefreshToken(accessToken);
        });
  }

  @Test
  @DisplayName("JwtUtil.decodeAccessToken() : 정상흐름")
  public void decodeAccessToken_ok() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    String givenToken = jwtUtil.createAccessToken(new AccessTokenData(givenId, givenRole));

    // when
    AccessTokenData resultTokenData = jwtUtil.decodeAccessToken(givenToken);

    // then
    assertEquals(givenId, resultTokenData.getId());
    assertEquals(givenRole, resultTokenData.getRoleType());
  }

  @Test
  @DisplayName("JwtUtil.decodeAccessToken() : 파손된 토큰")
  public void decodeAccessToken_damagedToken() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    String givenToken = jwtUtil.createAccessToken(new AccessTokenData(givenId, givenRole));
    String damegedToken = givenToken.substring(1);

    // when then
    assertThrows(
        JwtTokenException.class,
        () -> {
          jwtUtil.decodeAccessToken(damegedToken);
        });
  }

  @Test
  @DisplayName("JwtUtil.decodeAccessToken() : 유효기간이 지난 토큰")
  public void decodeAccessToken_expiredToken() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    ReflectionTestUtils.setField(jwtUtil, "accessExpirationTime", -100000L);
    String expiredToken = jwtUtil.createAccessToken(new AccessTokenData(givenId, givenRole));

    // when then
    assertThrows(
        JwtTokenException.class,
        () -> {
          jwtUtil.decodeAccessToken(expiredToken);
        });
  }

  @Test
  @DisplayName("JwtUtil.decodeAccessToken() : 타입이 다른 토큰")
  public void decodeAccessToken_incorrectType() {
    // given
    Long givenId = 3L;
    MemberRoleType givenRole = MemberRoleType.ROLE_MEMBER;
    String refreshToken = jwtUtil.createRefreshToken(new RefreshTokenData(givenId, givenRole));

    // when then
    assertThrows(
        JwtTokenException.class,
        () -> {
          jwtUtil.decodeAccessToken(refreshToken);
        });
  }
}
