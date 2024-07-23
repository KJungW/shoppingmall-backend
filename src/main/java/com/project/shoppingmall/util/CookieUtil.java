package com.project.shoppingmall.util;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
  public Cookie createCookie(String key, String value, int maxAge) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(maxAge);
    // cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setHttpOnly(true);

    return cookie;
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
