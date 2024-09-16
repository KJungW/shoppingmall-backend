package com.project.shoppingmall.exception;

public class CannotSaveBasketItemByOwnProduct extends RuntimeException {
  public CannotSaveBasketItemByOwnProduct(String message) {
    super(message);
  }
}
