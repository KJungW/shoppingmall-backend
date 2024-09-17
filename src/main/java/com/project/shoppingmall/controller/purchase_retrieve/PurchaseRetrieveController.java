package com.project.shoppingmall.controller.purchase_retrieve;

import com.project.shoppingmall.controller.purchase_retrieve.output.OutputRetrievePurchasesByBuyer;
import com.project.shoppingmall.controller.purchase_retrieve.output.OutputRetrieveSales;
import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.purchase.PurchaseItemDtoForSeller;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.service.purchase.PurchaseRetrieveService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemRetrieveService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PurchaseRetrieveController {
  private final PurchaseRetrieveService purchaseRetrieveService;
  private final PurchaseItemRetrieveService purchaseItemRetrieveService;

  @GetMapping("/purchases")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrievePurchasesByBuyer retrievePurchasesByBuyer(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Slice<Purchase> sliceResult =
        purchaseRetrieveService.retrieveAllByMember(userDetail.getId(), sliceNumber, sliceSize);
    return new OutputRetrievePurchasesByBuyer(sliceResult);
  }

  @GetMapping("/seller/product/purchases")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrieveSales retrieveSales(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("productId") Long productId) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SliceResult<PurchaseItemDtoForSeller> sliceResult =
        purchaseItemRetrieveService.retrieveAllForSeller(
            userDetail.getId(), productId, sliceNumber, sliceSize);
    return new OutputRetrieveSales(sliceResult);
  }

  @GetMapping("/seller/month/purchases")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrieveSales retrieveSalesInMonth(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("year") Integer year,
      @Range(min = 1, max = 12) @RequestParam("month") Integer month) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SliceResult<PurchaseItemDtoForSeller> sliceResult =
        purchaseItemRetrieveService.retrieveAllForSellerByDate(
            userDetail.getId(), year, month, sliceNumber, sliceSize);
    return new OutputRetrieveSales(sliceResult);
  }
}
