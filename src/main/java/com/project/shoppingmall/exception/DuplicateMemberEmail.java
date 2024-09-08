package com.project.shoppingmall.exception;

public class DuplicateMemberEmail extends RuntimeException {
  public DuplicateMemberEmail(String message) {
    super(message);
  }
}
