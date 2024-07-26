package com.project.shoppingmall.util;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
  @Value("${spring.profiles.default}")
  private String deployEnv;

  @Value("${frontend.domain}")
  private String domain;

  public ResponseCookie createCookie(String key, String value, int maxAge) {
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from(key, value)
            .maxAge(maxAge)
            .domain(domain)
            .path("/")
            .httpOnly(deployEnv.equals("prod"))
            .secure(!deployEnv.equals("dev"))
            .sameSite("Strict");
    return builder.build();
  }

  public String findCookie(String key, Cookie[] cookies) {
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(key)) {
        return cookie.getValue();
      }
    }
    return "";
  }
}
