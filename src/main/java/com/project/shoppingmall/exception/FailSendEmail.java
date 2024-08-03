package com.project.shoppingmall.exception;

public class FailSendEmail extends RuntimeException {
  public FailSendEmail(String message) {
    super(message);
  }
}
