package com.project.shoppingmall.controller.product;

import com.project.shoppingmall.controller.product.input.InputSaveProduct;
import com.project.shoppingmall.controller.product.input.InputUpdateProduct;
import com.project.shoppingmall.controller.product.output.OutputChangeProductToDiscontinued;
import com.project.shoppingmall.controller.product.output.OutputChangeProductToOnSale;
import com.project.shoppingmall.controller.product.output.OutputGetProduct;
import com.project.shoppingmall.controller.product.output.OutputSaveProduct;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.ProductDeleteService;
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
  private final ProductDeleteService productDeleteService;

  @GetMapping("/{productId}")
  public OutputGetProduct getProduct(@PathVariable("productId") Long productId) {
    Product product =
        productService
            .findByIdWithAll(productId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 제품을 찾을 수 없습니다."));
    return new OutputGetProduct(product);
  }

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
            .singleOptions(productData.getSingleOptions())
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

  @PutMapping()
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void updateProduct(
      @Valid @RequestPart("productData") InputUpdateProduct productData,
      @RequestPart(value = "productImages", required = false) List<MultipartFile> productImages,
      @RequestPart(value = "blockImages", required = false) List<MultipartFile> blockImages) {
    ProductMakeData productMakeData =
        ProductMakeData.builder()
            .productTypeId(productData.getProductTypeId())
            .name(productData.getName())
            .singleOptions(productData.getSingleOptions())
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
    productService.update(userDetail.getId(), productData.getProductId(), productMakeData);
  }

  @PutMapping("{productId}/sale-state/on-sale")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputChangeProductToOnSale ChangeProductToOnSale(
      @PathVariable("productId") Long productId) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Product product = productService.changeProductToOnSale(userDetail.getId(), productId);
    return new OutputChangeProductToOnSale(product.getId());
  }

  @PutMapping("{productId}/sale-state/discontinued")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputChangeProductToDiscontinued ChangeProductToDiscontinued(
      @PathVariable("productId") Long productId) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Product product = productService.changeProductToDiscontinued(userDetail.getId(), productId);
    return new OutputChangeProductToDiscontinued(product.getId());
  }

  @DeleteMapping("/{productId}")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void deleteProduct(@PathVariable("productId") Long productId) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    productDeleteService.deleteProductBySeller(userDetail.getId(), productId);
  }
}
