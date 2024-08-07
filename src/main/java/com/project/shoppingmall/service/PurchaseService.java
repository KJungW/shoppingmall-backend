package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.PurchaseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseService {

  private final MemberService memberService;
  private final BasketItemService basketItemService;
  private final PurchaseRepository purchaseRepository;

  public Purchase readyPurchase(
      Long memberId, List<PurchaseItemMakeData> purchaseItemMakeDataList, DeliveryDto deliveryDto) {
    Member buyer = loadMember(memberId);
    List<BasketItem> basketItems = loadBasketItems(purchaseItemMakeDataList);
    basketItemService.validateMemberIsBasketItemOwner(memberId, basketItems);

    ArrayList<PurchaseItem> purchaseItems = new ArrayList<>();
    for (BasketItem basketItem : basketItems) {
      PurchaseItemMakeData currentPurchaseItemMakeData =
          selectPurchaseMakeDataByBasketId(basketItem.getId(), purchaseItemMakeDataList);
      purchaseItems.add(makePurchaseItem(basketItem, currentPurchaseItemMakeData));
    }

    int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
    String purchaseUid = UUID.randomUUID().toString();
    String purchaseTitle =
        basketItems.get(0).getProduct().getName() + " 외 " + basketItems.size() + "개";
    DeliveryInfo deliveryInfo = new DeliveryInfo(deliveryDto);

    Purchase newPurchase =
        Purchase.builder()
            .buyer(buyer)
            .purchaseItems(purchaseItems)
            .purchaseUid(purchaseUid)
            .purchaseTitle(purchaseTitle)
            .deliveryInfo(deliveryInfo)
            .totalPrice(totalPrice)
            .build();

    purchaseRepository.save(newPurchase);
    return newPurchase;
  }

  private PurchaseItem makePurchaseItem(
      BasketItem basketItem, PurchaseItemMakeData purchaseItemMakeData) {
    BasketItemPriceCalcResult priceCalcResult =
        basketItemService.calculateBasketItemPrice(basketItem);

    if (!priceCalcResult.isOptionAvailable()) {
      throw new DataNotFound("유효하지 않은 장바구니 아이템입니다.");
    }

    int realPrice = priceCalcResult.getPrice();
    int expectedPrice = purchaseItemMakeData.getExpectedFinalPrice();
    if (realPrice != expectedPrice) {
      throw new DataNotFound("유효하지 않은 구매가격입니다");
    }

    Product product = basketItem.getProduct();
    ProductDataForPurchase productOptionObj =
        ProductDataForPurchase.builder()
            .productName(product.getName())
            .productTypeName(product.getProductType().getTypeName())
            .singleOption(priceCalcResult.getSingleOption())
            .multiOptions(priceCalcResult.getMultipleOptions())
            .price(product.getPrice())
            .discountAmount(product.getDiscountAmount())
            .discountRate(product.getDiscountRate())
            .build();

    return PurchaseItem.builder()
        .product(basketItem.getProduct())
        .productData(productOptionObj.makeJson())
        .finalPrice(realPrice)
        .build();
  }

  private PurchaseItemMakeData selectPurchaseMakeDataByBasketId(
      Long basketId, List<PurchaseItemMakeData> purchaseItemMakeDataList) {
    return purchaseItemMakeDataList.stream()
        .filter(makeData -> makeData.getBasketItemId() == basketId)
        .findFirst()
        .orElseThrow(() -> new ServerLogicError("PurchaseMakeData에 해당하는 BasketItem이 조회되지 않았습니다."));
  }

  private Member loadMember(Long memberId) {
    return memberService
        .findById(memberId)
        .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
  }

  private List<BasketItem> loadBasketItems(List<PurchaseItemMakeData> purchaseItemMakeDataList) {
    List<Long> basketIdList =
        purchaseItemMakeDataList.stream().map(PurchaseItemMakeData::getBasketItemId).toList();
    List<BasketItem> basketItems = basketItemService.findAllById(basketIdList);
    if (basketItems.size() != basketIdList.size())
      throw new DataNotFound("ID에 해당하는 제품이 존재하지 않습니다.");
    return basketItems;
  }
}
