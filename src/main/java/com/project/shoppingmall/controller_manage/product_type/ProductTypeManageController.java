package com.project.shoppingmall.controller_manage.product_type;

import com.project.shoppingmall.controller_manage.product_type.input.InputAddProductType;
import com.project.shoppingmall.controller_manage.product_type.input.InputUpdateProductType;
import com.project.shoppingmall.controller_manage.product_type.output.OutputAddProductType;
import com.project.shoppingmall.controller_manage.product_type.output.OutputUpdateProductType;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.service_manage.prodcut_type.ProductTypeManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductTypeManageController {
  private final ProductTypeManageService productTypeManageService;

  @PostMapping("/product/type")
  @PreAuthorize("hasRole('ROLE_ROOT_MANAGER')")
  public OutputAddProductType addProductType(@Valid @RequestBody InputAddProductType input) {
    ProductType newType = productTypeManageService.save(input.getTypeName());
    return new OutputAddProductType(newType.getId());
  }

  @PutMapping("/product/type")
  @PreAuthorize("hasRole('ROLE_ROOT_MANAGER')")
  public OutputUpdateProductType updateProductType(
      @Valid @RequestBody InputUpdateProductType input) {
    ProductType updatedType =
        productTypeManageService.update(input.getProductTypeId(), input.getTypeName());
    return new OutputUpdateProductType(updatedType.getId());
  }

  @DeleteMapping("/product/type")
  @PreAuthorize("hasRole('ROLE_ROOT_MANAGER')")
  public void deleteProductType(@RequestParam("productTypeId") Long productTypeId) {
    productTypeManageService.delete(productTypeId);
  }
}
