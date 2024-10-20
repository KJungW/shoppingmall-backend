package com.project.shoppingmall.service.basket_item;

import com.project.shoppingmall.dto.basket.BasketDto;
import com.project.shoppingmall.dto.basket.BasketItemDto;
import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasketItemRetrieveService {
  private final BasketItemService basketItemService;
  private final MemberFindService memberFindService;
  private final BasketItemRetrieveRepository basketItemRetrieveRepository;

  public BasketItemDto getBasketItemDetail(Long memberId, Long basketItemId) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원정보가 없습니다."));
    BasketItem basketItem =
        basketItemRetrieveRepository
            .retrieveBasketItemDetail(basketItemId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 장바구니 아이템이 존재하지 않습니다."));

    validateBasketItemOwner(basketItem, member);
    BasketItemPriceCalcResult basketItemPriceCalcResult =
        basketItemService.calculateBasketItemPrice(basketItem);
    return new BasketItemDto(basketItem, basketItemPriceCalcResult);
  }

  public BasketDto getBasket(Long memberId) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원정보가 없습니다."));
    List<BasketItem> basketItem = basketItemRetrieveRepository.retrieveBasketByMemberId(memberId);
    List<BasketItemDto> basketItemDtoList =
        basketItem.stream()
            .map(
                item -> {
                  BasketItemPriceCalcResult priceCalcResult =
                      basketItemService.calculateBasketItemPrice(item);
                  return new BasketItemDto(item, priceCalcResult);
                })
            .toList();
    return new BasketDto(basketItemDtoList);
  }

  private void validateBasketItemOwner(BasketItem basketItem, Member member) {
    if (!basketItem.getMember().getId().equals(member.getId())) {
      throw new DataNotFound("현재 회원에게 속해있지않은 장바구니 아이템입니다.");
    }
  }
}
