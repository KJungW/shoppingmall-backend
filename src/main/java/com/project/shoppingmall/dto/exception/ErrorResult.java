package com.project.shoppingmall.dto.exception;

import com.project.shoppingmall.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResult {
  ErrorCode Code;
  String message;
}
