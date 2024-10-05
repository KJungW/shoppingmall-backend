package com.project.shoppingmall.test_entity.product;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
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
                ProductSingleOptionBuilder.make(1L, 1000),
                ProductSingleOptionBuilder.make(2L, 2000),
                ProductSingleOptionBuilder.make(3L, 3000)));
    ArrayList<ProductMultipleOption> givenMultiOptions =
        new ArrayList<>(
            List.of(
                ProductMultiOptionBuilder.make(1L, 1000),
                ProductMultiOptionBuilder.make(2L, 2000),
                ProductMultiOptionBuilder.make(3L, 3000)));
    ArrayList<ProductImage> givenProductImageList =
        new ArrayList<>(
            List.of(
                ProductImage.builder().imageUri("test/uri1").downLoadUrl("test/down1").build(),
                ProductImage.builder().imageUri("test/uri2").downLoadUrl("test/down2").build(),
                ProductImage.builder().imageUri("test/uri3").downLoadUrl("test/down3").build()));
    List<ProductContent> givenContents =
        new ArrayList<>(
            List.of(
                ProductContentBuilder.makeImageContent(10L, 0L),
                ProductContentBuilder.makeTextContent(20L, 1L),
                ProductContentBuilder.makeImageContent(30L, 2L),
                ProductContentBuilder.makeImageContent(40L, 3L)));

    return Product.builder()
        .seller(MemberBuilder.makeMember(23141L))
        .productType(givenType)
        .name("testProduct")
        .price(2000)
        .discountAmount(100)
        .discountRate(10d)
        .isBan(false)
        .scoreAvg(0.0)
        .singleOptions(givenSingleOptions)
        .multipleOptions(givenMultiOptions)
        .productImages(givenProductImageList)
        .contents(givenContents);
  }

  public static Product.ProductBuilder lightData() {
    return Product.builder()
        .seller(MemberBuilder.makeMember(23141L))
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

  public static Product makeProduct(long id) {
    Product product = fullData().build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(long id, ProductType type) {
    Product product = fullData().productType(type).build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(long id, String name) {
    Product product = fullData().name(name).build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(long id, Member seller) {
    Product product = fullData().seller(seller).build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(long id, Member seller, boolean isBan) {
    Product product = fullData().seller(seller).isBan(isBan).build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(long id, Member seller, ProductType type) {
    Product product = fullData().seller(seller).productType(type).build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProduct(
      long id,
      Member seller,
      List<ProductSingleOption> singleOptions,
      List<ProductMultipleOption> multiOptions) {
    Product product =
        fullData()
            .seller(seller)
            .singleOptions(singleOptions)
            .multipleOptions(multiOptions)
            .build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static Product makeProductWithProductIdList(
      long id, Member seller, List<Long> singleOptionIdList, List<Long> multiOptionIdList) {
    List<ProductSingleOption> givenSingleOption =
        singleOptionIdList.stream().map(ProductSingleOptionBuilder::make).toList();
    List<ProductMultipleOption> givenMultiOption =
        multiOptionIdList.stream().map(ProductMultiOptionBuilder::make).toList();

    return ProductBuilder.makeProduct(id, seller, givenSingleOption, givenMultiOption);
  }

  public static Product makeProduct(
      long id, List<ProductSingleOption> singleOptions, List<ProductMultipleOption> multiOptions) {
    Product product = fullData().singleOptions(singleOptions).multipleOptions(multiOptions).build();
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
        fullData()
            .seller(seller)
            .price(price)
            .discountAmount(discountAmount)
            .discountRate(discountRate)
            .singleOptions(singleOptions)
            .multipleOptions(multiOptions)
            .build();
    ReflectionTestUtils.setField(product, "id", id);
    return product;
  }

  public static List<Product> makeProductList(List<Long> idList) {
    List<Product> productList = new ArrayList<>();
    idList.forEach(id -> productList.add(makeProduct(id)));
    return productList;
  }

  public static List<Product> makeProductList(List<Long> idList, ProductType type) {
    List<Product> productList = new ArrayList<>();
    idList.forEach(id -> productList.add(makeProduct(id, type)));
    return productList;
  }

  public static List<Product> makeProductList(List<Long> idList, String name) {
    List<Product> productList = new ArrayList<>();
    idList.forEach(id -> productList.add(makeProduct(id, name)));
    return productList;
  }

  public static List<Product> makeProductList(List<Long> idList, Member seller) {
    List<Product> productList = new ArrayList<>();
    idList.forEach(id -> productList.add(makeProduct(id, seller)));
    return productList;
  }
}
