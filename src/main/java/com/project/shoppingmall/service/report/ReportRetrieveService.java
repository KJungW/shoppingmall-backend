package com.project.shoppingmall.service.report;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.repository.ProductReportRetrieveRepository;
import com.project.shoppingmall.repository.ReviewReportRetrieveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportRetrieveService {
  private final ProductReportRetrieveRepository productReportRetrieveRepository;
  private final ReviewReportRetrieveRepository reviewReportRetrieveRepository;

  public Slice<ProductReport> findUnprocessedProductReport(
      long productTypeId, int sliceNum, int sliceSize) {
    PageRequest pageRequest =
        PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    return productReportRetrieveRepository.findUnprocessedProductReport(productTypeId, pageRequest);
  }

  public Slice<ReviewReport> findUnprocessedReviewReportReport(
      long productTypeId, int sliceNum, int sliceSize) {
    PageRequest pageRequest =
        PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    return reviewReportRetrieveRepository.findUnprocessedReviewReportReport(
        productTypeId, pageRequest);
  }
}
