package com.project.shoppingmall.testdata.product;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductBuilder {
  public static Product.ProductBuilder fullData() {
    ProductType givenType = new ProductType("test$type");
    ReflectionTestUtils.setField(givenType, "id", 1L);
    List<ProductSingleOption> givenSingleOptions =
        new ArrayList<>(
            List.of(
                ProductSingleOptionBuilder.makeProductSingleOption(1L, 1000),
                ProductSingleOptionBuilder.makeProductSingleOption(2L, 2000),
                ProductSingleOptionBuilder.makeProductSingleOption(3L, 3000)));
    ArrayList<ProductMultipleOption> givenMultiOptions =
        new ArrayList<>(
            List.of(
                ProductMultiOptionBuilder.makeProductMultiOption(1L, 1000),
                ProductMultiOptionBuilder.makeProductMultiOption(2L, 2000),
                ProductMultiOptionBuilder.makeProductMultiOption(3L, 3000)));
    ArrayList<ProductImage> givenProductImageList =
        new ArrayList<>(
            List.of(
                ProductImage.builder().imageUri("test/uri1").downLoadUrl("test/down1").build(),
                ProductImage.builder().imageUri("test/uri2").downLoadUrl("test/down2").build(),
                ProductImage.builder().imageUri("test/uri3").downLoadUrl("test/down3").build()));
    List<ProductContent> givenContents =
        new ArrayList<>(
            List.of(
                ProductContentBuilder.makeImageContent(1L),
                ProductContentBuilder.makeTextContent(21L),
                ProductContentBuilder.makeImageContent(3L),
                ProductContentBuilder.makeImageContent(4L)));

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

  public static Product makeProduct(long id) {
    Product product = lightData().build();
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
