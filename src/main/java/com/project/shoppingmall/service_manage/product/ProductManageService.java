package com.project.shoppingmall.service_manage.product;

import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.repository.ProductBulkRepository;
import com.project.shoppingmall.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductManageService {
  private final ProductBulkRepository productBulkRepository;
  private final ProductRepository productRepository;

  @Transactional
  public int banProductsBySellerId(long sellerId, boolean isBan) {
    return productBulkRepository.banProductsBySellerId(sellerId, isBan);
  }

  @Transactional
  public int changeProductTypeToBaseType(ProductType baseProductType, long targetProductTypeId) {
    return productBulkRepository.changeProductTypeToBaseType(baseProductType, targetProductTypeId);
  }

  public Slice<Product> findProductsByTypeInBatch(long typeId, int batchNumber, int batchSize) {
    PageRequest pageRequest = PageRequest.of(batchNumber, batchSize);
    return productRepository.findProductsByTypeInBatch(typeId, pageRequest);
  }
}
