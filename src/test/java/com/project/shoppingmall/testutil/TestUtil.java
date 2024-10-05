package com.project.shoppingmall.testutil;

import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductMultipleOption;
import com.project.shoppingmall.entity.ProductSingleOption;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

public class TestUtil {
  public static List<Long> makeIdList(int count, Long startId) {
    List<Long> idList = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      idList.add(startId + i);
    }
    return idList;
  }

  public static MockMultipartFile loadTestFile(String name, String path) {
    try {
      ClassPathResource resource = new ClassPathResource(path);
      InputStream inputStream = resource.getInputStream();
      String mimeType = URLConnection.guessContentTypeFromName(resource.getFilename());
      if (mimeType == null) {
        mimeType = URLConnection.guessContentTypeFromStream(inputStream);
      }
      return new MockMultipartFile(name, name, mimeType, inputStream);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static ProductSingleOption findSingleOptionInProduct(Product product, long targetId) {
    return product.getSingleOptions().stream()
        .filter(singleOption -> singleOption.getId().equals(targetId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("targetId에 해당하는 옵션을 찾을 수 없습니다."));
  }

  public static List<ProductMultipleOption> findMultiOptionsInProduct(
      Product product, List<Long> targetIdList) {
    List<ProductMultipleOption> result =
        product.getMultipleOptions().stream()
            .filter(dto -> targetIdList.contains(dto.getId()))
            .collect(Collectors.toList());
    if (targetIdList.size() != result.size())
      throw new IllegalArgumentException("targetId에 해당하는 옵션을 찾을 수 없습니다.");
    return result;
  }

  public static int calcProductPrice(
      Product product, ProductOptionDto singleOption, List<ProductOptionDto> multiOptions) {
    int productPrice =
        PriceCalculateUtil.calculatePrice(
            product.getPrice(), product.getDiscountAmount(), product.getDiscountRate());

    List<Integer> optionPriceList = new ArrayList<>();
    optionPriceList.add(singleOption.getPriceChangeAmount());
    optionPriceList.addAll(
        multiOptions.stream().map(ProductOptionDto::getPriceChangeAmount).toList());
    return PriceCalculateUtil.addOptionPrice(productPrice, optionPriceList);
  }
}
