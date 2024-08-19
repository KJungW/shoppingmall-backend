package com.project.shoppingmall.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
  public static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public static String encodePassword(String password) {
    return encoder.encode(password);
  }

  public static Boolean checkPassword(String targetPassword, String encodedPassword) {
    return encoder.matches(targetPassword, encodedPassword);
  }
}
