package com.project.shoppingmall.service.basket_item;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BasketItemDeleteService {
  private final BasketItemRepository basketItemRepository;
  private final MemberFindService memberFindService;

  public void deleteBasketItem(BasketItem basketItem) {
    if (basketItem == null) throw new ServerLogicError("비어있는 BasketItem을 제거하려고 시도하고 있습니다.");
    basketItemRepository.delete(basketItem);
  }

  public void deleteBasketItemList(List<BasketItem> basketItems) {
    basketItemRepository.deleteAllInBatch(basketItems);
  }

  public void deleteBasketItemInController(Long memberId, List<Long> basketItemIdList) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    List<BasketItem> findAllResult = basketItemRepository.findAllById(basketItemIdList);

    if (findAllResult.size() != basketItemIdList.size())
      throw new DataNotFound("ID에 해당하는 장바구니 아이템이 존재하지 않습니다.");

    findAllResult.forEach(
        basketItem -> {
          if (!basketItem.getMember().getId().equals(member.getId()))
            throw new DataNotFound("현재 회원은 장바구니 아이템의 주인이 아닙니다.");
        });

    deleteBasketItemList(findAllResult);
  }
}
