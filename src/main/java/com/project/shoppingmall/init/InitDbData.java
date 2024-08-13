package com.project.shoppingmall.init;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.repository.*;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class InitDbData {
  @Value("${spring.config.activate.on-profile}")
  private String envType;

  @Value("${spring.jpa.hibernate.ddl-auto}")
  private String ddlType;

  private final ProductTypeRepository productTypeRepository;
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;
  private final PurchaseRepository purchaseRepository;
  private final RefundRepository refundRepository;

  @PostConstruct
  public void init() {
    if (envType.equals("dev") || envType.equals("prod")) {
      if (ddlType.equals("create") || ddlType.equals("create-drop")) {
        initData();
      }
    }
  }

  private void initData() {
    Random randomGenerator = new Random();

    // 제품 타입 생성
    ProductType type1 = productTypeRepository.save(new ProductType("게임$타이틀"));
    ProductType type2 = productTypeRepository.save(new ProductType("게임$게임기"));
    ProductType type3 = productTypeRepository.save(new ProductType("도서$소설"));
    ProductType type4 = productTypeRepository.save(new ProductType("도서$만화책"));
    ProductType type5 = productTypeRepository.save(new ProductType("식표품$과일"));
    ProductType type6 = productTypeRepository.save(new ProductType("식표품$밀키트"));
    List<ProductType> typeList =
        new ArrayList<>(Arrays.asList(type1, type2, type3, type4, type5, type6));

    // 회원 생성
    Member seller =
        Member.builder()
            .loginType(LoginType.NAVER)
            .socialId("testMemberSocialId123123421aaa")
            .nickName("seller")
            .profileImageUrl("init/member_profile_img.png")
            .profileImageDownLoadUrl(
                "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/member_profile_img.png")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    memberRepository.save(seller);
    Member buyer =
        Member.builder()
            .loginType(LoginType.NAVER)
            .socialId("testMemberSocialIdcvv90498s0df")
            .nickName("buyer")
            .profileImageUrl("init/member_profile_img.png")
            .profileImageDownLoadUrl(
                "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/member_profile_img.png")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    memberRepository.save(buyer);

    // 타입별로 30개의 제품 생성
    List<Product> productListInType1 = new ArrayList<>();
    for (ProductType type : typeList) {
      String productName = type.getTypeName().replace("$", "-");
      for (int i = 0; i < 30; i++) {
        Product product =
            Product.builder()
                .seller(seller)
                .productType(type)
                .productImages(new ArrayList<>())
                .contents(new ArrayList<>())
                .singleOptions(new ArrayList<>())
                .multipleOptions(new ArrayList<>())
                .name(productName + "-임시제품-" + i)
                .price((randomGenerator.nextInt(2) + 1) * 100)
                .discountAmount(randomGenerator.nextInt(6))
                .discountRate(randomGenerator.nextInt(2) * 10.0)
                .isBan(false)
                .scoreAvg((double) randomGenerator.nextInt(6))
                .build();

        ProductImage productImg1 =
            ProductImage.builder()
                .imageUri("init/product_img1.jpg")
                .downLoadUrl(
                    "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_img1.jpg")
                .build();
        ProductImage productImg2 =
            ProductImage.builder()
                .imageUri("init/product_img2.jpg")
                .downLoadUrl(
                    "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_img2.jpg")
                .build();
        ProductImage productImg3 =
            ProductImage.builder()
                .imageUri("init/product_img3.jpg")
                .downLoadUrl(
                    "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_img3.jpg")
                .build();
        List<ProductImage> productImages =
            new ArrayList<>(Arrays.asList(productImg1, productImg2, productImg3));
        product.updateProductImages(productImages);

        ProductContent content1 =
            ProductContent.builder()
                .type(BlockType.TEXT_TYPE)
                .content(JsonUtil.convertObjectToJson(new TextBlock(1L, "임시적인 텍스트블록입니다")))
                .build();
        ProductContent content2 =
            ProductContent.builder()
                .type(BlockType.IMAGE_TYPE)
                .content(
                    JsonUtil.convertObjectToJson(
                        new ImageBlock(
                            2L,
                            "init/product_content_img1.jpg",
                            "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_content_img1.jpg")))
                .build();
        ProductContent content3 =
            ProductContent.builder()
                .type(BlockType.IMAGE_TYPE)
                .content(
                    JsonUtil.convertObjectToJson(
                        new ImageBlock(
                            3L,
                            "init/product_content_img2.jpg",
                            "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_content_img2.jpg")))
                .build();
        ProductContent content4 =
            ProductContent.builder()
                .type(BlockType.IMAGE_TYPE)
                .content(
                    JsonUtil.convertObjectToJson(
                        new ImageBlock(
                            4L,
                            "init/product_content_img3.jpg",
                            "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_content_img3.jpg")))
                .build();
        List<ProductContent> productContents =
            new ArrayList<>(Arrays.asList(content1, content2, content3, content4));
        product.updateContents(productContents);

        ProductSingleOption singleOption1 =
            ProductSingleOption.builder().optionName("임시 단일옵션1").priceChangeAmount(5).build();
        ProductSingleOption singleOption2 =
            ProductSingleOption.builder().optionName("임시 단일옵션2").priceChangeAmount(5).build();
        ProductSingleOption singleOption3 =
            ProductSingleOption.builder().optionName("임시 단일옵션3").priceChangeAmount(5).build();
        List<ProductSingleOption> singleOptions =
            new ArrayList<>(Arrays.asList(singleOption1, singleOption2, singleOption3));
        product.updateSingleOption(singleOptions);

        ProductMultipleOption multiOption1 =
            ProductMultipleOption.builder().optionName("임시 다중옵션1").priceChangeAmount(5).build();
        ProductMultipleOption multiOption2 =
            ProductMultipleOption.builder().optionName("임시 다중옵션2").priceChangeAmount(5).build();
        ProductMultipleOption multiOption3 =
            ProductMultipleOption.builder().optionName("임시 다중옵션3").priceChangeAmount(5).build();
        List<ProductMultipleOption> multiOptions =
            new ArrayList<>(Arrays.asList(multiOption1, multiOption2, multiOption3));
        product.updateMultiOptions(multiOptions);
        productRepository.save(product);

        if (type.getTypeName().equals(type1.getTypeName())) productListInType1.add(product);
      }
    }

    // 50개의 구매데이터 생성
    List<Purchase> purchaseList = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      List<PurchaseItem> purchaseItems = new ArrayList<>();
      for (int k = 0; k < 3; k++) {
        Product targetProduct = productListInType1.get(k);
        ProductDataForPurchase productOptionObj =
            ProductDataForPurchase.builder()
                .productId(targetProduct.getId())
                .sellerId(targetProduct.getSeller().getId())
                .sellerName(targetProduct.getSeller().getNickName())
                .productName(targetProduct.getName())
                .productTypeName(targetProduct.getProductType().getTypeName())
                .price(targetProduct.getPrice())
                .discountAmount(targetProduct.getDiscountAmount())
                .discountRate(targetProduct.getDiscountRate())
                .build();
        PurchaseItem purchaseItem =
            PurchaseItem.builder()
                .product(targetProduct)
                .productData(productOptionObj.makeJson())
                .finalPrice(targetProduct.getFinalPrice())
                .build();
        purchaseItems.add(purchaseItem);
      }
      int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
      DeliveryInfo deliveryInfo =
          new DeliveryInfo(buyer.getNickName(), "test address", "11011", "101-0000-0000");
      Purchase purchase =
          Purchase.builder()
              .buyer(buyer)
              .purchaseItems(purchaseItems)
              .purchaseUid(i + "testPurchaseUid1234")
              .purchaseTitle("임시구매" + i)
              .deliveryInfo(deliveryInfo)
              .totalPrice(totalPrice)
              .build();
      purchase.convertStateToComplete(i + "testPaymentUid1234");
      purchaseRepository.save(purchase);

      if (i < 30) purchaseList.add(purchase);
    }

    // 30의 구매데이터에 대해 환불 데이터 생성
    for (Purchase purchase : purchaseList) {
      PurchaseItem targetItem = purchase.getPurchaseItems().get(0);
      Refund refund =
          Refund.builder()
              .refundPrice(targetItem.getFinalPrice())
              .requestTitle("환불이 필요합니다.")
              .requestContent("환불을 해주시면 감사하겠습니다.")
              .build();
      targetItem.addRefund(refund);
      refund.acceptRefund("환불을 승인합니다. 반품을 진행해주세요");
      refund.completeRefund();
      refundRepository.save(refund);
    }
  }
}
