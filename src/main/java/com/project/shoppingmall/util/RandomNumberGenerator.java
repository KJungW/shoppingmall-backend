package com.project.shoppingmall.util;

import com.project.shoppingmall.exception.ServerLogicError;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomNumberGenerator {
  public String makeRandomNumber() {
    try {
      String result;
      do {
        int num = SecureRandom.getInstanceStrong().nextInt(999999);
        result = String.valueOf(num);
      } while (result.length() != 6);
      return result;
    } catch (NoSuchAlgorithmException ex) {
      throw new ServerLogicError("잘못된 난수생성 구성입니다.");
    }
  }
}
