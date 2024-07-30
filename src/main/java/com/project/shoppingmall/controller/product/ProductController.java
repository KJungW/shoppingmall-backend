package com.project.shoppingmall.controller.product;

import com.project.shoppingmall.controller.product.input.InputSaveProduct;
import com.project.shoppingmall.controller.product.output.OutputSaveProduct;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @PostMapping()
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputSaveProduct saveProduct(
      @Valid @RequestPart("productData") InputSaveProduct productData,
      @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages,
      @RequestPart(value = "blockImages", required = false) List<MultipartFile> blockImages) {
    ProductMakeData productMakeData =
        ProductMakeData.builder()
            .productTypeId(productData.getProductTypeId())
            .name(productData.getName())
            .singleOption(productData.getSingleOption())
            .multiOptions(productData.getMultiOptions())
            .blockDataList(productData.getBlockDataList())
            .price(productData.getPrice())
            .discountAmount(productData.getDiscountAmount())
            .discountRate(productData.getDiscountRate())
            .productImages(productImages)
            .blockImages(blockImages)
            .build();
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Product savedProduct = productService.save(userDetail.getId(), productMakeData);
    return new OutputSaveProduct(savedProduct.getId());
  }
}
