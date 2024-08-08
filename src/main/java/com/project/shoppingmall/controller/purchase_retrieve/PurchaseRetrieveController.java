package com.project.shoppingmall.controller.purchase_retrieve;

import com.project.shoppingmall.controller.purchase_retrieve.output.OutputFindAllByMember;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.service.PurchaseRetrieveService;
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

  @GetMapping("/purchases")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputFindAllByMember retrieveAllByMember(
      @PositiveOrZero @RequestParam("sliceNumber") int sliceNumber,
      @Positive @RequestParam("sliceSize") int sliceSize) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Slice<Purchase> sliceResult =
        purchaseRetrieveService.retrieveAllByMember(userDetail.getId(), sliceNumber, sliceSize);
    return new OutputFindAllByMember(sliceResult);
  }
}
