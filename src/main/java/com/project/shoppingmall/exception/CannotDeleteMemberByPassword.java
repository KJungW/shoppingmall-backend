package com.project.shoppingmall.exception;

public class CannotDeleteMemberByPassword extends RuntimeException {
  public CannotDeleteMemberByPassword(String message) {
    super(message);
  }
}
