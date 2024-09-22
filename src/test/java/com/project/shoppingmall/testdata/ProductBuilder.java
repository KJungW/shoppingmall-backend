package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.type.BlockType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductBuilder {
  public static final Integer PRODUCT_IMAGE_COUNT = 3;
  public static final Integer TEXT_BLOCK_COUNT = 1;
  public static final Integer IMAGE_BLOCK_COUNT = 3;

  public static Product.ProductBuilder fullData() {
    ProductType givenType = new ProductType("test$type");
    ReflectionTestUtils.setField(givenType, "id", 1L);
    List<ProductSingleOption> givenSingleOptions =
        new ArrayList() {
          {
            add(
                ProductSingleOption.builder()
                    .optionName("singleOption1")
                    .priceChangeAmount(1000)
                    .build());
            add(
                ProductSingleOption.builder()
                    .optionName("singleOption2")
                    .priceChangeAmount(2000)
                    .build());
            add(
                ProductSingleOption.builder()
                    .optionName("singleOption3")
                    .priceChangeAmount(3000)
                    .build());
          }
        };

    ArrayList<ProductMultipleOption> givenMultiOptions =
        new ArrayList() {
          {
            add(
                ProductMultipleOption.builder()
                    .optionName("multiOption1")
                    .priceChangeAmount(1000)
                    .build());
            add(
                ProductMultipleOption.builder()
                    .optionName("multiOption2")
                    .priceChangeAmount(2000)
                    .build());
            add(
                ProductMultipleOption.builder()
                    .optionName("multiOption3")
                    .priceChangeAmount(3000)
                    .build());
          }
        };
    ArrayList<ProductImage> givenProductImageList =
        new ArrayList() {
          {
            add(ProductImage.builder().imageUri("test/uri1").downLoadUrl("test/down1").build());
            add(ProductImage.builder().imageUri("test/uri2").downLoadUrl("test/down2").build());
            add(ProductImage.builder().imageUri("test/uri3").downLoadUrl("test/down3").build());
          }
        };

    List<ProductContent> givenContents =
        new ArrayList() {
          {
            add(
                ProductContent.builder()
                    .type(BlockType.IMAGE_TYPE)
                    .content("contentJson1")
                    .build());
            add(ProductContent.builder().type(BlockType.TEXT_TYPE).content("contentJson2").build());
            add(
                ProductContent.builder()
                    .type(BlockType.IMAGE_TYPE)
                    .content("contentJson3")
                    .build());
            add(
                ProductContent.builder()
                    .type(BlockType.IMAGE_TYPE)
                    .content("contentJson4")
                    .build());
          }
        };

    return Product.builder()
        .seller(MemberBuilder.fullData().build())
        .productType(givenType)
        .name("testProduct")
        .price(2000)
        .discountAmount(100)
        .discountRate(10.5)
        .isBan(false)
        .scoreAvg(0.0)
        .singleOptions(givenSingleOptions)
        .multipleOptions(givenMultiOptions)
        .productImages(givenProductImageList)
        .contents(givenContents);
  }

  public static Product.ProductBuilder lightData() {
    return Product.builder()
        .seller(MemberBuilder.fullData().build())
        .productType(new ProductType("test$type"))
        .name("testProduct")
        .price(2000)
        .discountAmount(100)
        .discountRate(10.5)
        .isBan(false)
        .scoreAvg(0.0)
        .singleOptions(new ArrayList<>())
        .multipleOptions(new ArrayList<>())
        .productImages(new ArrayList<>())
        .contents(new ArrayList<>());
  }

  public static Product makeNoBannedProduct(Member seller, ProductType type) {
    return ProductBuilder.lightData().seller(seller).productType(type).isBan(false).build();
  }

  public static Product makeProduct(long id, Member seller) {
    Product product =
        Product.builder()
            .seller(seller)
            .productType(new ProductType("test$type"))
            .name("testProduct" + id)
            .price(2000)
            .discountAmount(100)
            .discountRate(10.5)
            .isBan(false)
            .scoreAvg(0.0)
            .singleOptions(new ArrayList<>())
            .multipleOptions(new ArrayList<>())
            .productImages(new ArrayList<>())
            .contents(new ArrayList<>())
            .build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(
      long id,
      Member seller,
      List<ProductSingleOption> singleOptions,
      List<ProductMultipleOption> multiOptions) {
    Product product =
        Product.builder()
            .seller(seller)
            .productType(new ProductType("test$type"))
            .name("testProduct" + id)
            .price(2000)
            .discountAmount(100)
            .discountRate(10.5)
            .isBan(false)
            .scoreAvg(0.0)
            .singleOptions(singleOptions)
            .multipleOptions(multiOptions)
            .productImages(new ArrayList<>())
            .contents(new ArrayList<>())
            .build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(
      long id,
      Member seller,
      int price,
      int discountAmount,
      double discountRate,
      List<ProductSingleOption> singleOptions,
      List<ProductMultipleOption> multiOptions) {
    Product product =
        Product.builder()
            .seller(seller)
            .productType(new ProductType("test$type"))
            .name("testProduct" + id)
            .price(price)
            .discountAmount(discountAmount)
            .discountRate(discountRate)
            .isBan(false)
            .scoreAvg(0.0)
            .singleOptions(singleOptions)
            .multipleOptions(multiOptions)
            .productImages(new ArrayList<>())
            .contents(new ArrayList<>())
            .build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }
}
