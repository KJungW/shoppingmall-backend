package com.project.shoppingmall.service.purchase;

import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.exception.*;
import com.project.shoppingmall.repository.PurchaseRepository;
import com.project.shoppingmall.service.basket_item.BasketItemService;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.refund.RefundService;
import com.project.shoppingmall.type.PaymentResultType;
import com.project.shoppingmall.type.PurchaseStateType;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseService {

  private final MemberService memberService;
  private final BasketItemService basketItemService;
  private final PurchaseRepository purchaseRepository;
  private final IamportClient iamportClient;
  private final RefundService refundService;

  @Transactional
  public Purchase readyPurchase(
      Long memberId, List<PurchaseItemMakeData> purchaseItemMakeDataList, DeliveryDto deliveryDto) {
    Member buyer = loadMember(memberId);
    List<BasketItem> basketItems = loadBasketItems(purchaseItemMakeDataList);
    basketItemService.validateMemberIsBasketItemOwner(memberId, basketItems);

    if (buyer.getIsBan()) throw new CannotPurchaseBecauseMemberBan("벤상태의 회원은 제품을 구매할 수 없습니다.");

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

  @Transactional
  public PaymentResultType completePurchase(String purchaseUid, String paymentUid) {

    Purchase purchase =
        findByPurchaseUid(purchaseUid)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 구매데이터가 존재하지 않습니다."));
    if (purchase.getState() != PurchaseStateType.READY) {
      return PaymentResultType.ALREADY_PROCESSED;
    }

    IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(paymentUid);
    if (paymentResponse == null) {
      throw new DataNotFound("잘못된 paymentUid 입니다.");
    }
    Payment realPaymentData = paymentResponse.getResponse();

    if (!realPaymentData.getStatus().equals("paid")) {
      purchase.convertStateToFail(paymentUid);
      return PaymentResultType.FAIL_OR_CANCEL;
    }

    int expectedPurchasePrice = purchase.getTotalPrice();
    int realPurchasePrice = realPaymentData.getAmount().intValue();
    if (expectedPurchasePrice != realPurchasePrice) {
      purchase.convertStateToDetectPriceTampering(paymentUid);
      refundService.processRefund(paymentUid, realPurchasePrice);
      return PaymentResultType.DETECTION_PRICE_TAMPERING;
    }

    purchase.convertStateToComplete(paymentUid);
    return PaymentResultType.COMPLETE;
  }

  public Optional<Purchase> findByPurchaseUid(String purchaseUid) {
    return purchaseRepository.findByPurchaseUid(purchaseUid);
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
            .productId(product.getId())
            .sellerId(product.getSeller().getId())
            .sellerName(product.getSeller().getNickName())
            .productName(product.getName())
            .productTypeName(product.getProductType().getTypeName())
            .singleOption(priceCalcResult.getSingleOption())
            .multiOptions(priceCalcResult.getMultipleOptions())
            .price(product.getPrice())
            .discountAmount(product.getDiscountAmount())
            .discountRate(product.getDiscountRate())
            .build();

    return PurchaseItem.builder().productData(productOptionObj).finalPrice(realPrice).build();
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
