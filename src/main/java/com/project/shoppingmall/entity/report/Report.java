package com.project.shoppingmall.entity.report;

import com.project.shoppingmall.entity.BaseEntity;
import com.project.shoppingmall.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "REPORT_TYPE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Report extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REPORTER_ID")
  private Member reporter;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TARGET_MEMBER_ID")
  private Member targetMember;

  private String title;
  private String description;
  private boolean isProcessedComplete;

  public Report(
      Member reporter,
      Member targetMember,
      String title,
      String description,
      boolean isProcessedComplete) {
    this.reporter = reporter;
    this.targetMember = targetMember;
    this.title = title;
    this.description = description;
    this.isProcessedComplete = isProcessedComplete;
  }
}
