package com.project.shoppingmall.exception;

public class ServerLogicError extends RuntimeException {
  public ServerLogicError(String message) {
    super(message);
  }
}
