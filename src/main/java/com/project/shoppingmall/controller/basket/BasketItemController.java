package com.project.shoppingmall.controller.basket;

import com.project.shoppingmall.controller.basket.input.InputSaveBasketItem;
import com.project.shoppingmall.controller.basket.output.OutputSaveBasketItem;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.dto.basket.BasketDto;
import com.project.shoppingmall.dto.basket.BasketItemDto;
import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.service.BasketItemRetrieveService;
import com.project.shoppingmall.service.BasketItemService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member/basket")
@RequiredArgsConstructor
public class BasketItemController {
  private final BasketItemService basketItemService;
  private final BasketItemRetrieveService basketItemRetrieveService;

  @GetMapping("/{basketItemId}")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public BasketItemDto getBasketItem(@PathVariable("basketItemId") Long basketId) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return basketItemRetrieveService.getBasketItemDetail(userDetail.getId(), basketId);
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public BasketDto getBasket() {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return basketItemRetrieveService.getBasket(userDetail.getId());
  }

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

  @DeleteMapping
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void deleteBasketItem(
      @Valid @RequestParam("basketItemIdList") List<Long> basketItemIdList) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    basketItemService.deleteBasketItem(userDetail.getId(), basketItemIdList);
  }
}
