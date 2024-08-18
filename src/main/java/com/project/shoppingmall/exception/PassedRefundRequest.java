package com.project.shoppingmall.exception;

public class PassedRefundRequest extends RuntimeException {
  public PassedRefundRequest(String message) {
    super(message);
  }
}
