package com.project.shoppingmall.controller.product_retrieve;

import com.project.shoppingmall.controller.product_retrieve.output.OutputGetProductsBySearchWordWithFilter;
import com.project.shoppingmall.controller.product_retrieve.output.OutputGetProductsByTypeWithFilter;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.service.ProductRetrieveService;
import com.project.shoppingmall.type.ProductRetrieveFilterType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class ProductRetrieveController {
  private final ProductRetrieveService productRetrieveService;

  @GetMapping("{productTypeId}/products")
  public OutputGetProductsByTypeWithFilter getProductsByTypeWithFilter(
      @PathVariable("productTypeId") Long productId,
      @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("sliceNumber") Integer sliceNumber,
      @RequestParam("filterType") ProductRetrieveFilterType filterType) {
    Slice<Product> sliceResult =
        productRetrieveService.retrieveByTypeWithFilter(
            productId, sliceSize, sliceNumber, filterType);
    return new OutputGetProductsByTypeWithFilter(sliceResult);
  }

  @GetMapping("/products")
  public OutputGetProductsBySearchWordWithFilter getProductsBySearchWordWithFilter(
      @RequestParam("searchWord") String searchWord,
      @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("sliceNumber") Integer sliceNumber,
      @RequestParam("filterType") ProductRetrieveFilterType filterType) {
    Slice<Product> sliceResult =
        productRetrieveService.retrieveBySearchWordWithFilter(
            searchWord, sliceSize, sliceNumber, filterType);
    return new OutputGetProductsBySearchWordWithFilter(sliceResult);
  }
}
