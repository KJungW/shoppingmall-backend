package com.project.shoppingmall.dto.refund;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.type.RefundStateTypeForPurchaseItem;
import com.project.shoppingmall.util.JsonUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefundPurchaseItemForSeller {
  private long purchaseId;
  private long buyerId;
  private String buyerName;
  private DeliveryDto deliveryInfo;
  private LocalDateTime purchaseDateTime;

  private long purchaseItemId;
  private long productId;
  private long sellerId;
  private String sellerName;
  private String productName;
  private String productTypeName;
  private ProductOptionDto selectedSingleOption;
  private List<ProductOptionDto> selectedMultiOptions;
  private int price;
  private int discountAmount;
  private double discountRate;
  private int finalPrice;
  private boolean isRefund;
  private RefundStateTypeForPurchaseItem refundState;

  public RefundPurchaseItemForSeller(PurchaseItem item) {
    Purchase purchase = item.getPurchase();
    this.purchaseId = purchase.getId();
    this.buyerId = purchase.getBuyer().getId();
    this.buyerName = purchase.getBuyer().getNickName();
    this.deliveryInfo = new DeliveryDto(purchase.getDeliveryInfo());
    this.purchaseDateTime = purchase.getCreateDate();

    ProductDataForPurchase productData =
        JsonUtil.convertJsonToObject(item.getProductData(), ProductDataForPurchase.class);
    this.purchaseItemId = item.getId();
    this.productId = productData.getProductId();
    this.sellerId = productData.getSellerId();
    this.sellerName = productData.getSellerName();
    this.productName = productData.getProductName();
    this.productTypeName = productData.getProductTypeName();
    this.selectedSingleOption = productData.getSingleOption();
    this.selectedMultiOptions = productData.getMultiOptions();
    this.price = productData.getPrice();
    this.discountAmount = productData.getDiscountAmount();
    this.discountRate = productData.getDiscountRate();
    this.finalPrice = item.getFinalPrice();
    this.isRefund = item.getIsRefund();
    this.refundState = item.getFinalRefundState();
  }
}
