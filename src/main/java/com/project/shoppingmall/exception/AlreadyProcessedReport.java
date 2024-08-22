package com.project.shoppingmall.exception;

public class AlreadyProcessedReport extends RuntimeException {
  public AlreadyProcessedReport(String message) {
    super(message);
  }
}
