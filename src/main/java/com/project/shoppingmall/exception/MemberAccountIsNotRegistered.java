package com.project.shoppingmall.exception;

public class MemberAccountIsNotRegistered extends RuntimeException {
  public MemberAccountIsNotRegistered(String message) {
    super(message);
  }
}
