package com.project.shoppingmall.test_entity.product;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.entity.ProductContent;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.JsonUtil;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductContentBuilder {
  public static ProductContent makeImageContent(long id, long index) {
    ImageBlock imageBlock = new ImageBlock(index, "test/Uri", "test/downloadUrl");
    String blockDataJson = JsonUtil.convertObjectToJson(imageBlock);
    ProductContent content =
        ProductContent.builder().type(BlockType.IMAGE_TYPE).content(blockDataJson).build();
    ReflectionTestUtils.setField(content, "id", id);
    return content;
  }

  public static ProductContent makeTextContent(long id, long index) {
    TextBlock textBlock = new TextBlock(index, "content");
    String blockDataJson = JsonUtil.convertObjectToJson(textBlock);
    ProductContent content =
        ProductContent.builder().type(BlockType.TEXT_TYPE).content(blockDataJson).build();
    ReflectionTestUtils.setField(content, "id", id);
    return content;
  }
}
