package com.project.shoppingmall.exception;

public class CannotDeleteMemberByRefund extends RuntimeException {
  public CannotDeleteMemberByRefund(String message) {
    super(message);
  }
}
