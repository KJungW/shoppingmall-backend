package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.exception.ContinuousReportError;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ProductReportRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {
  private final ProductService productService;
  private final MemberService memberService;
  private final ProductReportRepository productReportRepository;

  @Transactional
  public void saveProductReport(long memberId, long productId, String title, String description) {
    Member reporter =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 제품이 존재하지 않습니다."));

    if (checkProductReportIsWithinOneDay(memberId, productId)) {
      throw new ContinuousReportError("연속으로 신고를 진행할 수 없습니다. 24시간이 지난뒤에 신고해주세요");
    }

    ProductReport report =
        ProductReport.builder()
            .reporter(reporter)
            .targetMember(product.getSeller())
            .title(title)
            .description(description)
            .product(product)
            .build();
    productReportRepository.save(report);
  }

  private boolean checkProductReportIsWithinOneDay(Long memberId, Long productId) {
    PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createDate"));
    List<ProductReport> latestReport =
        productReportRepository.findLatestReport(memberId, productId, pageRequest);
    if (!latestReport.isEmpty()) {
      LocalDateTime reportCreatedDate = latestReport.get(0).getCreateDate();
      return reportCreatedDate.isAfter(LocalDateTime.now().minusDays(1));
    }
    return false;
  }
}
