package com.project.shoppingmall.exception;

public class MemberSignupByEmailCacheError extends RuntimeException {
  public MemberSignupByEmailCacheError(String message) {
    super(message);
  }
}
