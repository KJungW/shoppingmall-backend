package com.project.shoppingmall.controller.product_retrieve;

import com.project.shoppingmall.controller.product_retrieve.output.OutputGetProductBySeller;
import com.project.shoppingmall.controller.product_retrieve.output.OutputGetProductsBySearchWordWithFilter;
import com.project.shoppingmall.controller.product_retrieve.output.OutputGetProductsByTypeWithFilter;
import com.project.shoppingmall.controller.product_retrieve.output.OutputGetRandomProducts;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.service.product.ProductRetrieveService;
import com.project.shoppingmall.type.ProductRetrieveFilterType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class ProductRetrieveController {
  private final ProductRetrieveService productRetrieveService;

  @GetMapping("seller/products")
  public OutputGetProductBySeller getProductsBySeller(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("sellerId") Long sellerId) {
    Slice<Product> sliceResult =
        productRetrieveService.retrieveBySeller(sellerId, sliceNumber, sliceSize);
    return new OutputGetProductBySeller(sliceResult);
  }

  @GetMapping("{productTypeId}/products")
  public OutputGetProductsByTypeWithFilter getProductsByTypeWithFilter(
      @PathVariable("productTypeId") Long productId,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @RequestParam("filterType") ProductRetrieveFilterType filterType) {
    Slice<Product> sliceResult =
        productRetrieveService.retrieveByTypeWithFilter(
            productId, sliceSize, sliceNumber, filterType);
    return new OutputGetProductsByTypeWithFilter(sliceResult);
  }

  @GetMapping("/products")
  public OutputGetProductsBySearchWordWithFilter getProductsBySearchWordWithFilter(
      @RequestParam("searchWord") String searchWord,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @RequestParam("filterType") ProductRetrieveFilterType filterType) {
    Slice<Product> sliceResult =
        productRetrieveService.retrieveBySearchWordWithFilter(
            searchWord, sliceSize, sliceNumber, filterType);
    return new OutputGetProductsBySearchWordWithFilter(sliceResult);
  }

  @GetMapping("/products/random")
  public OutputGetRandomProducts getRandomProducts(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize) {
    Slice<Product> sliceResult = productRetrieveService.retrieveByRandom(sliceNumber, sliceSize);
    return new OutputGetRandomProducts(sliceResult);
  }
}
