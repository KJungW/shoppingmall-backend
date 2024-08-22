package com.project.shoppingmall.service.report;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ProductReportRetrieveRepository;
import com.project.shoppingmall.repository.ReviewReportRetrieveRepository;
import com.project.shoppingmall.service.member.MemberService;
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
  private final MemberService memberService;
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

  public Slice<ProductReport> findProductReportsByProductSeller(
      long productSellerId, int sliceNum, int sliceSize) {
    memberService
        .findById(productSellerId)
        .orElseThrow(() -> new DataNotFound("id에 해당하는 제품판매자가 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    return productReportRetrieveRepository.findProductReportsByProductSeller(
        productSellerId, pageRequest);
  }

  public Slice<ReviewReport> findReviewReportsByReviewWriter(
      long reviewWriterId, int sliceNum, int sliceSize) {
    memberService
        .findById(reviewWriterId)
        .orElseThrow(() -> new DataNotFound("id에 해당하는 리뷰작성자가 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    return reviewReportRetrieveRepository.findReviewReportsByReviewWriter(
        reviewWriterId, pageRequest);
  }
}
