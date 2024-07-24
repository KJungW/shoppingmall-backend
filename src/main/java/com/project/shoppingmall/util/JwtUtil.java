package com.project.shoppingmall.util;

import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.type.JwtTokenType;
import com.project.shoppingmall.type.MemberRoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  @Value("${jwt.refresh.expiration_time}")
  private Long refreshExpirationTime;

  @Value("${jwt.access.expiration_time}")
  private Long accessExpirationTime;

  private SecretKey secretKey;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    secretKey =
        new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
  }

  public Long getRefreshExpirationTimeMs() {
    return refreshExpirationTime;
  }

  public Long getAccessExpirationTimeMs() {
    return accessExpirationTime;
  }

  public String createRefreshToken(RefreshTokenData data) {
    return Jwts.builder()
        .claim("type", JwtTokenType.REFRESH)
        .claim("id", data.getId())
        .claim("role", data.getRoleType())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
        .signWith(secretKey)
        .compact();
  }

  public String createAccessToken(AccessTokenData data) {
    return Jwts.builder()
        .claim("type", JwtTokenType.ACCESS)
        .claim("id", data.getId())
        .claim("role", data.getRoleType())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + accessExpirationTime))
        .signWith(secretKey)
        .compact();
  }

  public RefreshTokenData decodeRefreshToken(String token) throws JwtTokenException {
    try {
      Claims payload =
          Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

      JwtTokenType tokenType = JwtTokenType.valueOf(payload.get("type", String.class));
      if (!tokenType.equals(JwtTokenType.REFRESH))
        throw new JwtTokenException("refresh Token이 아닌 다른 타입의 토큰입니다.");

      Long id = payload.get("id", Long.class);
      MemberRoleType role = MemberRoleType.valueOf(payload.get("role", String.class));
      return new RefreshTokenData(id, role);

    } catch (io.jsonwebtoken.JwtException | IllegalArgumentException | JwtTokenException ex) {
      throw new JwtTokenException("유효하지 않은 토큰입니다.");
    }
  }

  public AccessTokenData decodeAccessToken(String token) throws JwtTokenException {
    try {
      Claims payload =
          Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

      JwtTokenType tokenType = JwtTokenType.valueOf(payload.get("type", String.class));
      if (!tokenType.equals(JwtTokenType.ACCESS))
        throw new JwtTokenException("access Token이 아닌 다른 타입의 토큰입니다.");

      Long id = payload.get("id", Long.class);
      MemberRoleType role = MemberRoleType.valueOf(payload.get("role", String.class));
      return new AccessTokenData(id, role);

    } catch (io.jsonwebtoken.JwtException | IllegalArgumentException | JwtTokenException ex) {
      throw new JwtTokenException("유효하지 않은 토큰입니다.");
    }
  }
}
