package com.project.shoppingmall.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordEncoderUtilTest {

  @Test
  @DisplayName("encodePassword(), checkPassword() : 정상흐름")
  public void encodePassword_ok() {
    String givenPassword = "daf14214oscx%@#$";
    String encodedPassword = PasswordEncoderUtil.encodePassword(givenPassword);
    assertTrue(PasswordEncoderUtil.checkPassword(givenPassword, encodedPassword));
  }

  @Test
  @DisplayName("checkPassword() : 잘못된 비밀번호 입력")
  public void checkPassword_invalidPassword() {
    String givenPassword = "daf14214oscx%@#$";
    String encodedPassword = PasswordEncoderUtil.encodePassword(givenPassword);

    String wrongPassword = "xccxvlkjoiwje15%!";
    assertFalse(PasswordEncoderUtil.checkPassword(wrongPassword, encodedPassword));
  }
}
