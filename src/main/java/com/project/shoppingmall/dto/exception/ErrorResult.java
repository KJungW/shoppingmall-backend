package com.project.shoppingmall.dto.exception;

import com.project.shoppingmall.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResult {
  ErrorCode code;
  String message;
}
