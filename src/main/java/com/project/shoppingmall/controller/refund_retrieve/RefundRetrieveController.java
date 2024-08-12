package com.project.shoppingmall.controller.refund_retrieve;

import com.project.shoppingmall.controller.refund_retrieve.output.OutputFinaAllByBuyer;
import com.project.shoppingmall.controller.refund_retrieve.output.OutputFindAllBySeller;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.service.PurchaseItemRetrieveService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundRetrieveController {
  private final PurchaseItemRetrieveService purchaseItemRetrieveService;

  @GetMapping("member/purchase/refunds")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputFinaAllByBuyer findAllByBuyer(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Slice<PurchaseItem> sliceResult =
        purchaseItemRetrieveService.retrieveRefundedAllForBuyer(
            userDetail.getId(), sliceNumber, sliceSize);
    return new OutputFinaAllByBuyer(sliceResult);
  }

  @GetMapping("member/product/refunds")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputFindAllBySeller findAllBySeller(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Slice<PurchaseItem> sliceResult =
        purchaseItemRetrieveService.retrieveRefundedAllForSeller(
            userDetail.getId(), sliceNumber, sliceSize);
    return new OutputFindAllBySeller(sliceResult);
  }
}
