package com.project.shoppingmall.controller.basket;

import com.project.shoppingmall.controller.basket.input.InputSaveBasketItem;
import com.project.shoppingmall.controller.basket.output.OutputSaveBasketItem;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.basket.BasketDto;
import com.project.shoppingmall.dto.basket.BasketItemDto;
import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.service.basket_item.BasketItemDeleteService;
import com.project.shoppingmall.service.basket_item.BasketItemRetrieveService;
import com.project.shoppingmall.service.basket_item.BasketItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
  private final BasketItemDeleteService basketItemDeleteService;

  @GetMapping("/{basketItemId}")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public BasketItemDto getBasketItem(@PathVariable("basketItemId") Long basketId) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return basketItemRetrieveService.getBasketItemDetail(userDetail.getId(), basketId);
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public BasketDto getBasket() {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return basketItemRetrieveService.getBasket(userDetail.getId());
  }

  @PostMapping
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputSaveBasketItem saveBasketItem(@Valid @RequestBody InputSaveBasketItem input) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
      @Size(min = 1) @RequestParam("basketItemIdList") List<Long> basketItemIdList) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    basketItemDeleteService.deleteBasketItemInController(userDetail.getId(), basketItemIdList);
  }
}
