package com.project.shoppingmall.controller.refund_retrieve;

import com.project.shoppingmall.controller.refund_retrieve.output.OutputFinaAllByBuyer;
import com.project.shoppingmall.controller.refund_retrieve.output.OutputFindAllAboutPurchaseItem;
import com.project.shoppingmall.controller.refund_retrieve.output.OutputFindAllBySeller;
import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.refund.RefundDto;
import com.project.shoppingmall.dto.refund.RefundPurchaseItemForBuyer;
import com.project.shoppingmall.dto.refund.RefundPurchaseItemForSeller;
import com.project.shoppingmall.service.purchase_item.PurchaseItemRetrieveService;
import com.project.shoppingmall.service.refund.RefundRetrieveService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundRetrieveController {
  private final PurchaseItemRetrieveService purchaseItemRetrieveService;
  private final RefundRetrieveService refundRetrieveService;

  @GetMapping("member/purchase/refunds")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputFinaAllByBuyer findAllByBuyer(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SliceResult<RefundPurchaseItemForBuyer> sliceResult =
        purchaseItemRetrieveService.retrieveRefundedAllForBuyer(
            userDetail.getId(), sliceNumber, sliceSize);
    return new OutputFinaAllByBuyer(sliceResult);
  }

  @GetMapping("member/product/refunds")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputFindAllBySeller findAllBySeller(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SliceResult<RefundPurchaseItemForSeller> sliceResult =
        purchaseItemRetrieveService.retrieveRefundedAllForSeller(
            userDetail.getId(), sliceNumber, sliceSize);
    return new OutputFindAllBySeller(sliceResult);
  }

  @GetMapping("member/purchaseItem/refunds")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputFindAllAboutPurchaseItem findAllAboutPurchaseItem(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize,
      @RequestParam("purchaseItemId") long purchaseItemId) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SliceResult<RefundDto> sliceResult =
        refundRetrieveService.retrieveAllByPurchaseItem(
            userDetail.getId(), purchaseItemId, sliceNumber, sliceSize);
    return new OutputFindAllAboutPurchaseItem(sliceResult);
  }
}
