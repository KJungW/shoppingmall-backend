package com.project.shoppingmall.handler;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.exception.*;
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

  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(ServerLogicError.class)
  public ErrorResult WrongServerLogicExceptionHandler(ServerLogicError e) {
    return new ErrorResult(ErrorCode.SERVER_ERROR, "예상치 못한 서버 오류입니다.");
  }

  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(FailSendEmail.class)
  public ErrorResult MessageExceptionHandler(FailSendEmail e) {
    return new ErrorResult(ErrorCode.EMAIL_SEND_FAIL, "이메일 전송에 실패했습니다. 잠시후 다시 시도해주세요.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(EmailRegistrationCacheError.class)
  public ErrorResult EmailRegistrationCacheExceptionHandler(EmailRegistrationCacheError e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "이메일 등록 유효시간이 지났거나, 데이터가 유효하지 않습니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DataNotFound.class)
  public ErrorResult DataNotFoundExceptionHandler(DataNotFound e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "데이터가 존재하진 않습니다.");
  }

  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(FileUploadFail.class)
  public ErrorResult FileUploadFailExceptionHandler(FileUploadFail e) {
    return new ErrorResult(ErrorCode.SERVER_ERROR, "파일업로드에 실패했습니다. 잠시후 다시 시도해주세요");
  }

  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(FileDeleteFail.class)
  public ErrorResult FileDeleteFailExceptionHandler(FileDeleteFail e) {
    return new ErrorResult(ErrorCode.SERVER_ERROR, "파일삭제에 실패했습니다. 잠시후 다시 시도해주세요");
  }
}
