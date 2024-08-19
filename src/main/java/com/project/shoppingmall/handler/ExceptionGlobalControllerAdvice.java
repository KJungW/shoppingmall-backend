package com.project.shoppingmall.handler;

import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.exception.*;
import com.project.shoppingmall.exception.RecentlyPurchasedProduct;
import com.project.shoppingmall.type.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
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
    return new ErrorResult(ErrorCode.BAD_INPUT, "데이터가 존재하지 않습니다.");
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

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @InitBinder
  public void BindExceptionHandler(WebDataBinder binder) {
    if (binder.getBindingResult().hasErrors()) {
      throw new InputDataBindingError("입력값이 잘못되었습니다. 한번더 확인해주세요");
    }
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InputDataBindingError.class)
  public ErrorResult InputDataBindingErrorHandler(InputDataBindingError e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "입력값이 잘못되었습니다. 한번더 확인해주세요");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResult MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "입력값이 잘못되었습니다. 한번더 확인해주세요");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(NotMatchBlockAndImage.class)
  public ErrorResult NotMatchBlockAndImageExceptionHandler(NotMatchBlockAndImage e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "이미지블록과 이미지 파일이 매칭되지 않습니다.");
  }

  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(InvalidEnumType.class)
  public ErrorResult InvalidEnumTypeExceptionHandler(InvalidEnumType e) {
    return new ErrorResult(ErrorCode.SERVER_ERROR, "예상치 못한 서버에러입니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(WrongPriceAndDiscount.class)
  public ErrorResult WrongPriceAndDiscountExceptionHandler(WrongPriceAndDiscount e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "가격과 할인이 적절하지 않습니다. 다시 한버 확인해주세요");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ContinuousReportError.class)
  public ErrorResult ContinuousReportErrorHandler(ContinuousReportError e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "연속으로 신고를 진행할 수 없습니다. 24시간이 지난뒤에 신고해주세요");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ProcessOrCompleteRefund.class)
  public ErrorResult ProcessOrCompleteRefundErrorHandler(ProcessOrCompleteRefund e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "이미 진행중이거나 완료된 환불처리가 있습니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(NotRequestStateRefund.class)
  public ErrorResult NotRequestStateRefundErrorHandler(NotRequestStateRefund e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "요청상태의 환불에만 환불요청을 수행할 수 있습니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(NotAcceptStateRefund.class)
  public ErrorResult NotAcceptStateRefundErrorHandler(NotAcceptStateRefund e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "승인이 완료된 환불에 대해서만 환불 완료요청을 수행할 수 있습니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(FailRefundException.class)
  public ErrorResult FailRefundExceptionHandler(FailRefundException e) {
    return new ErrorResult(ErrorCode.REFUND_FAIL, "환불이 실패했습니다. 잠시후 다시 시도해주세요!");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(AddBannedProductInBasket.class)
  public ErrorResult AddBannedProductInBasketHandler(AddBannedProductInBasket e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "벤처리된 제품은 장바구니에 담을 수 없습니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(AddDiscontinuedProductInBasket.class)
  public ErrorResult AddDiscontinuedProductInBasketHandler(AddDiscontinuedProductInBasket e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "판매중단된 제품은 장바구니에 담을 수 없습니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(RecentlyPurchasedProduct.class)
  public ErrorResult RecentlyPurchasedProductHandler(RecentlyPurchasedProduct e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "정해진 일자 이내에 구매기록이 없는 제품만 삭제가능합니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(PassedRefundRequest.class)
  public ErrorResult PassedRefundRequestHandler(PassedRefundRequest e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "정해진 일자 이내에 구매기록이 없는 제품만 삭제가능합니다.");
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(AlreadyExistReview.class)
  public ErrorResult AlreadyExistReviewHandler(AlreadyExistReview e) {
    return new ErrorResult(ErrorCode.BAD_INPUT, "이미 현재 구매 아이템에 대해 작성한 리뷰가 이미 존재합니다.");
  }
}
