package com.project.shoppingmall.dto.report;

import com.project.shoppingmall.entity.report.ReviewReport;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewReportDto {
  private Long reportId;
  private LocalDateTime reportCreatedDate;
  private Long reporterId;
  private String reporterName;
  private String title;
  private String description;
  private Boolean isProcessedComplete;
  private Long reviewId;
  private String reviewTitle;
  private Long writer;
  private String writerName;

  public ReviewReportDto(ReviewReport report) {
    this.reportId = report.getId();
    this.reportCreatedDate = report.getCreateDate();
    this.reporterId = report.getReporter().getId();
    this.reporterName = report.getReporter().getNickName();
    this.title = report.getTitle();
    this.description = report.getDescription();
    this.isProcessedComplete = report.isProcessedComplete();
    this.reviewId = report.getReview().getId();
    this.reviewTitle = report.getReview().getTitle();
    this.writer = report.getReview().getWriter().getId();
    this.writerName = report.getReview().getWriter().getNickName();
  }
}
