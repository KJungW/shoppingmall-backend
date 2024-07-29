package com.project.shoppingmall.controller.product_type;

import com.project.shoppingmall.controller.product_type.output.OutputGetAllProductType;
import com.project.shoppingmall.controller.product_type.output.OutputGetProductType;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.ProductTypeService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductTypeController {
  private final ProductTypeService productTypeService;

  @GetMapping("/type/{typeId}")
  public OutputGetProductType getProductType(@PathVariable("typeId") Long typeId) {
    ProductType productType =
        productTypeService
            .findById(typeId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    return new OutputGetProductType(productType);
  }

  @GetMapping("/types")
  public OutputGetAllProductType getAllProductType() {
    List<ProductType> allProductTypes = productTypeService.getAllProductType();
    List<OutputGetProductType> outputList =
        allProductTypes.stream().map(OutputGetProductType::new).collect(Collectors.toList());
    return new OutputGetAllProductType(outputList);
  }
}
