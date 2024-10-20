package com.project.shoppingmall.controller_manage.report_retrieve.output;

import com.project.shoppingmall.dto.report.ProductReportDto;
import com.project.shoppingmall.entity.report.ProductReport;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
public class OutputRetrieveProductReportByType {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<ProductReportDto> productReportList;

  public OutputRetrieveProductReportByType(Slice<ProductReport> sliceResult) {
    this.currentSliceNumber = sliceResult.getNumber();
    this.sliceSize = sliceResult.getSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.hasNext();
    this.hasPrevious = sliceResult.hasPrevious();
    this.productReportList = sliceResult.getContent().stream().map(ProductReportDto::new).toList();
  }
}
