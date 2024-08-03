package com.project.shoppingmall.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class RandomNumberGeneratorTest {
  public RandomNumberGenerator randomNumberGenerator;

  @BeforeEach
  public void beforeEach() {
    randomNumberGenerator = new RandomNumberGenerator();
  }

  @RepeatedTest(5)
  @DisplayName("RandomNumberGenerator.makeRandomNumber() : 정상흐름")
  public void makeRandomNumber_ok() {
    // when
    String randomNum = randomNumberGenerator.makeRandomNumber();

    // then
    assertEquals(6, randomNum.length());
  }
}
