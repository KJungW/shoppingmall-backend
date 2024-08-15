package com.project.shoppingmall.exception;

public class AlreadyDeletedProduct extends RuntimeException {
  public AlreadyDeletedProduct(String message) {
    super(message);
  }
}
