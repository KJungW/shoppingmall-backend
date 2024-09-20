package com.project.shoppingmall.controller.purchase.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputReadyPurchase {
  @NotNull @Valid private List<InputBasketItem> basketItems;
  @NotNull @Valid private InputDeliveryInfo deliveryInfo;
}
