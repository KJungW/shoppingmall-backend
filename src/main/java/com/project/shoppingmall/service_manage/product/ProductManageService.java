package com.project.shoppingmall.service_manage.product;

import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.repository.ProductBulkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductManageService {
  private final ProductBulkRepository productBulkRepository;

  @Transactional
  public int changeProductTypeToBaseType(ProductType baseProductType, long targetProductTypeId) {
    return productBulkRepository.changeProductTypeToBaseType(baseProductType, targetProductTypeId);
  }
}
