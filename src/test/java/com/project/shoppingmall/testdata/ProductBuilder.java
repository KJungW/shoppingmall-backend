package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.type.BlockType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductBuilder {
  public static final Integer PRODUCT_IMAGE_COUNT = 3;
  public static final Integer TextBlockCount = 1;
  public static final Integer ImageBlockCount = 3;

  public static Product.ProductBuilder fullData() throws IOException {
    ProductType givenType = new ProductType("test/type");
    ReflectionTestUtils.setField(givenType, "id", 1L);
    ProductSingleOption givenSingleOption =
        ProductSingleOption.builder().optionName("singleOption").priceChangeAmount(-1000).build();
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
    List<ProductContent> givenBlockImages =
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
        .singleOption(givenSingleOption)
        .multipleOptions(givenMultiOptions)
        .productImages(givenProductImageList)
        .contents(givenBlockImages);
  }
}
