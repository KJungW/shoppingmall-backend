package com.project.shoppingmall.final_value;

public class AlarmContentTemplate {
  private static final String ALARM_MEMBER_BAN_TRUE = "현재 회원이 벤되었습니다.";
  private static final String ALARM_MEMBER_BAN_FALSE = "회원의 벤이 취소되었습니다.";
  private static final String ALARM_REVIEW_BAN_TRUE = "\"%s\" 리뷰가 벤되었습니다.";
  private static final String ALARM_REVIEW_BAN_FALSE = "\"%s\" 리뷰의 벤이 취소되었습니다.";
  private static final String ALARM_PRODUCT_BAN_TRUE = "\"%s\" 제품이 벤되었습니다.";
  private static final String ALARM_PRODUCT_BAN_FALSE = "\"%s\" 제품의 벤이 취소되었습니다.";
  private static final String ALARM_TYPE_DELETE = "기존의 카테고리가 삭제되면서 \"%s\" 제품의 카테고리가 변경되었습니다.";
  private static final String ALARM_TYPE_UPDATE = "기존의 카테고리가 변경되면서 \"%s\" 제품의 카테고리가 변경되었습니다.";
  private static final String ALARM_REFUND_REQUEST = "판매제품에 대한 환불요청 도착했습니다.";

  public static String makeMemberBanAlarmContent(boolean isBan) {
    if (isBan) return ALARM_MEMBER_BAN_TRUE;
    else return ALARM_MEMBER_BAN_FALSE;
  }

  public static String makeReviewBanAlarmContent(boolean isBan, String reviewTitle) {
    if (isBan) return String.format(ALARM_REVIEW_BAN_TRUE, reviewTitle);
    else return String.format(ALARM_REVIEW_BAN_FALSE, reviewTitle);
  }

  public static String makeProductBanAlarmContent(boolean isBan, String productName) {
    if (isBan) return String.format(ALARM_PRODUCT_BAN_TRUE, productName);
    else return String.format(ALARM_PRODUCT_BAN_FALSE, productName);
  }

  public static String makeTypeDeleteAlarmContent(String productName) {
    return String.format(ALARM_TYPE_DELETE, productName);
  }

  public static String makeTypeUpdateAlarmContent(String productName) {
    return String.format(ALARM_TYPE_UPDATE, productName);
  }

  public static String makeRefundRequestAlarmContent() {
    return ALARM_REFUND_REQUEST;
  }
}
