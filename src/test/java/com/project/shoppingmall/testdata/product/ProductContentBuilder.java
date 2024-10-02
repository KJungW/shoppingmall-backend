package com.project.shoppingmall.testdata.product;

import com.project.shoppingmall.entity.ProductContent;
import com.project.shoppingmall.type.BlockType;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductContentBuilder {
  public static ProductContent makeImageContent(long id) {
    ProductContent content =
        ProductContent.builder().type(BlockType.IMAGE_TYPE).content("contentJson").build();
    ReflectionTestUtils.setField(content, "id", id);
    return content;
  }

  public static ProductContent makeTextContent(long id) {
    ProductContent content =
        ProductContent.builder().type(BlockType.TEXT_TYPE).content("contentJson").build();
    ReflectionTestUtils.setField(content, "id", id);
    return content;
  }
}
