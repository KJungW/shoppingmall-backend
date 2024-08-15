package com.project.shoppingmall.entity.report;

import com.project.shoppingmall.entity.BaseEntity;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Report extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REPORTER_ID")
  private Member reporter;

  private String title;
  private String description;
  private boolean isProcessedComplete;

  public Report(Member reporter, String title, String description) {
    updateReporter(reporter);
    updateReportContent(title, description);
    updateIsProcessedComplete(false);
  }

  private void updateReporter(Member reporter) {
    if (reporter == null) throw new ServerLogicError("Report의 reporter에 빈값이 들어왔습니다.");
    this.reporter = reporter;
  }

  public void updateReportContent(String title, String description) {
    if (title == null || title.isBlank() || description == null || description.isBlank())
      throw new ServerLogicError("Report의 title과 description에 빈값이 들어왔습니다.");
    this.title = title;
    this.description = description;
  }

  public void updateIsProcessedComplete(Boolean isProcessedComplete) {
    if (isProcessedComplete == null)
      throw new ServerLogicError("Report의 title과 isProcessedComplete에 빈값이 들어왔습니다.");
    this.isProcessedComplete = isProcessedComplete;
  }
}
