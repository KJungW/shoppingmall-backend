package com.project.shoppingmall.exception;

public class CannotSaveProductBecauseMemberBan extends RuntimeException {
  public CannotSaveProductBecauseMemberBan(String message) {
    super(message);
  }
}
