package com.project.shoppingmall.dto.purchase;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.util.JsonUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseItemDtoForSeller {
  private long buyerId;
  private DeliveryDto deliveryInfo;
  private LocalDateTime dateTime;
  private Long purchaseItemId;
  private long productId;
  private ProductOptionDto selectedSingleOption;
  private List<ProductOptionDto> selectedMultiOptions;
  private int price;
  private int discountAmount;
  private double discountRate;
  private int finalPrice;
  private boolean isRefund;

  public PurchaseItemDtoForSeller(PurchaseItem purchaseItem) {
    Purchase targetPurchase = purchaseItem.getPurchase();
    ProductDataForPurchase productData =
        JsonUtil.convertJsonToObject(purchaseItem.getProductData(), ProductDataForPurchase.class);
    this.buyerId = targetPurchase.getBuyer().getId();
    this.deliveryInfo = new DeliveryDto(targetPurchase.getDeliveryInfo());
    this.dateTime = targetPurchase.getCreateDate();
    this.purchaseItemId = purchaseItem.getId();
    this.productId = purchaseItem.getProduct().getId();
    this.selectedSingleOption = productData.getSingleOption();
    this.selectedMultiOptions = productData.getMultiOptions();
    this.price = productData.getPrice();
    this.discountAmount = productData.getDiscountAmount();
    this.discountRate = productData.getDiscountRate();
    this.finalPrice = purchaseItem.getFinalPrice();
    this.isRefund = purchaseItem.isRefund();
  }
}
