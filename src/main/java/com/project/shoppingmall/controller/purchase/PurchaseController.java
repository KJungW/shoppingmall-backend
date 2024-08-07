package com.project.shoppingmall.controller.purchase;

import com.project.shoppingmall.controller.purchase.input.InputCompletePurchase;
import com.project.shoppingmall.controller.purchase.input.InputReadyPurchase;
import com.project.shoppingmall.controller.purchase.output.OutputCompletePurchase;
import com.project.shoppingmall.controller.purchase.output.OutputReadyPurchase;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.service.PurchaseService;
import com.project.shoppingmall.type.PaymentResultType;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {
  private final PurchaseService purchaseService;

  @PostMapping()
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputReadyPurchase readyPurchase(@Valid @RequestBody InputReadyPurchase input) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    List<PurchaseItemMakeData> purchaseItemMakeDataList =
        input.getBasketItems().stream()
            .map(
                basketItem ->
                    new PurchaseItemMakeData(
                        basketItem.getBasketItemId(), basketItem.getExpectedPrice()))
            .toList();
    DeliveryDto deliveryDto = input.getDeliveryInfo().makeDeliveryDto();
    Purchase purchase =
        purchaseService.readyPurchase(userDetail.getId(), purchaseItemMakeDataList, deliveryDto);
    return new OutputReadyPurchase(
        purchase.getPurchaseUid(),
        purchase.getPurchaseTitle(),
        new DeliveryDto(purchase.getDeliveryInfo()),
        purchase.getTotalPrice());
  }

  @PostMapping("/payment")
  public OutputCompletePurchase completePurchase(@Valid @RequestBody InputCompletePurchase input) {
    PaymentResultType result =
        purchaseService.completePurchase(input.getPurchaseUid(), input.getPaymentUid());
    return new OutputCompletePurchase(result);
  }
}
