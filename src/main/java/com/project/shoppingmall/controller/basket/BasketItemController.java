package com.project.shoppingmall.controller.basket;

import com.project.shoppingmall.controller.basket.input.InputSaveBasketItem;
import com.project.shoppingmall.controller.basket.output.OutputSaveBasketItem;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.service.BasketItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/basket")
@RequiredArgsConstructor
public class BasketItemController {
  private final BasketItemService basketItemService;

  @PostMapping
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputSaveBasketItem saveBasketItem(@Valid @RequestBody InputSaveBasketItem input) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BasketItemMakeData basketItemMakeData =
        BasketItemMakeData.builder()
            .memberId(userDetail.getId())
            .productId(input.getProductId())
            .singleOptionId(input.getSingleOptionId())
            .multipleOptionId(input.getMultipleOptionId())
            .build();
    BasketItem basketItem = basketItemService.saveBasketItem(basketItemMakeData);
    return new OutputSaveBasketItem(basketItem.getId());
  }
}
