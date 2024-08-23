package com.project.shoppingmall.service_manage.prodcut_type;

import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ProductTypeRepository;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeManageService {
  private final ProductTypeService productTypeService;
  private final ProductTypeRepository productTypeRepository;

  @Transactional
  public ProductType save(String typeName) {
    return productTypeRepository.save(new ProductType(typeName));
  }

  @Transactional
  public ProductType update(long productTypeId, String typeName) {
    ProductType productType =
        productTypeService
            .findById(productTypeId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 ProductType이 존재하지 않습니다."));
    productType.updateTypeName(typeName);
    return productType;
  }
}
