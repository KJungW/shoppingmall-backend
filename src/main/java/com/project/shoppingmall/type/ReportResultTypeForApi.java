package com.project.shoppingmall.type;

public enum ReportResultTypeForApi {
  NO_ACTION,
  TARGET_BAN,
  MEMBER_BAN;

  // 변환 메서드
  public ReportResultType toReportResultType() {
    return switch (this) {
      case NO_ACTION -> ReportResultType.NO_ACTION;
      case TARGET_BAN -> ReportResultType.TARGET_BAN;
      case MEMBER_BAN -> ReportResultType.MEMBER_BAN;
    };
  }
}
