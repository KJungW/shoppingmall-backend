package com.project.shoppingmall.controller.purchase.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputReadyPurchase {
  @NotNull
  @Size(min = 1)
  @Valid
  private List<InputBasketItem> basketItems;

  @NotNull @Valid private InputDeliveryInfo deliveryInfo;
}
