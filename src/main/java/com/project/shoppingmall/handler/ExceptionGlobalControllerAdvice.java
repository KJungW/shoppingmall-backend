package com.project.shoppingmall.handler;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.exception.TokenNotFound;
import com.project.shoppingmall.type.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionGlobalControllerAdvice {
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(TokenNotFound.class)
  public ErrorResult WrongInputExceptionHandler(TokenNotFound e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "토큰이 존재하지 않습니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(JwtTokenException.class)
  public ErrorResult WrongInputExceptionHandler(JwtTokenException e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "토큰이 유효하지 않습니다.");
  }
}
