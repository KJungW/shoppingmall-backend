package com.project.shoppingmall.controller.purchase_retrieve;

import com.project.shoppingmall.controller.purchase_retrieve.output.OutputRetrievePurchaseBySeller;
import com.project.shoppingmall.controller.purchase_retrieve.output.OutputRetrievePurchasesByBuyer;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.service.purchase.PurchaseRetrieveService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemRetrieveService;
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
public class PurchaseRetrieveController {
  private final PurchaseRetrieveService purchaseRetrieveService;
  private final PurchaseItemRetrieveService purchaseItemRetrieveService;

  @GetMapping("/purchases")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrievePurchasesByBuyer retrievePurchasesByBuyer(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Slice<Purchase> sliceResult =
        purchaseRetrieveService.retrieveAllByMember(userDetail.getId(), sliceNumber, sliceSize);
    return new OutputRetrievePurchasesByBuyer(sliceResult);
  }

  @GetMapping("/seller/product/purchases")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrievePurchaseBySeller retrievePurchasesBySeller(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize,
      @RequestParam("productId") long productId) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Slice<PurchaseItem> sliceResult =
        purchaseItemRetrieveService.retrieveAllForSeller(
            userDetail.getId(), productId, sliceNumber, sliceSize);
    return new OutputRetrievePurchaseBySeller(sliceResult);
  }
}
