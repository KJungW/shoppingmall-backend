package com.project.shoppingmall.controller_manage.report_retrieve.output;

import com.project.shoppingmall.dto.report.ReviewReportDto;
import com.project.shoppingmall.entity.report.ReviewReport;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
public class OutputRetrieveReviewReportsByReviewWriter {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<ReviewReportDto> reviewReportList;

  public OutputRetrieveReviewReportsByReviewWriter(Slice<ReviewReport> sliceResult) {
    this.currentSliceNumber = sliceResult.getNumber();
    this.sliceSize = sliceResult.getSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.hasNext();
    this.hasPrevious = sliceResult.hasPrevious();
    this.reviewReportList = sliceResult.getContent().stream().map(ReviewReportDto::new).toList();
  }
}
