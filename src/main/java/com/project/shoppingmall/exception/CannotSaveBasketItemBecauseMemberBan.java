package com.project.shoppingmall.exception;

public class CannotSaveBasketItemBecauseMemberBan extends RuntimeException {
  public CannotSaveBasketItemBecauseMemberBan(String message) {
    super(message);
  }
}
