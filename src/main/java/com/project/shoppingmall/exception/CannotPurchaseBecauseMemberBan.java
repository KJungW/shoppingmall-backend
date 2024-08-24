package com.project.shoppingmall.exception;

public class CannotPurchaseBecauseMemberBan extends RuntimeException {
  public CannotPurchaseBecauseMemberBan(String message) {
    super(message);
  }
}
