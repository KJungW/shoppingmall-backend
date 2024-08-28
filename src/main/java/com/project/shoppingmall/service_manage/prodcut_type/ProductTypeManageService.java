package com.project.shoppingmall.service_manage.prodcut_type;

import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.exception.CannotDeleteBaseProductType;
import com.project.shoppingmall.exception.CannotUpdateBaseProductType;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ProductTypeRepository;
import com.project.shoppingmall.service.EntityManagerService;
import com.project.shoppingmall.service.alarm.AlarmService;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import com.project.shoppingmall.service_manage.product.ProductManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeManageService {
  private final ProductTypeService productTypeService;
  private final ProductTypeRepository productTypeRepository;
  private final ProductManageService productManageService;
  private final AlarmService alarmService;
  private final EntityManagerService entityManagerService;

  @Value("${spring.jpa.properties.hibernate.default_batch_fetch_size}")
  private int batchSize;

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
    if (productType.checkBaseProductType())
      throw new CannotUpdateBaseProductType("기본 제품 타입을 삭제할 수 없습니다.");

    int batchNum = 0;
    Slice<Product> productBatch;
    do {
      productBatch =
          productManageService.findProductsByTypeInBatch(productType.getId(), batchNum, batchSize);
      alarmService.makeAllTypeUpdateAlarm(productBatch.getContent());
      batchNum++;
    } while (productBatch.hasNext());

    productType.updateTypeName(typeName);
    return productType;
  }

  @Transactional
  public void delete(long productTypeId) {
    ProductType productType =
        productTypeService
            .findById(productTypeId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 ProductType이 존재하지 않습니다."));
    ProductType baseProductType =
        productTypeService
            .findBaseProductType()
            .orElseThrow(() -> new DataNotFound("id에 해당하는 BaseProductType이 존재하지 않습니다."));
    if (productType.checkBaseProductType())
      throw new CannotDeleteBaseProductType("기본 제품 타입을 삭제할 수 없습니다.");

    int batchNum = 0;
    Slice<Product> productBatch;
    do {
      productBatch =
          productManageService.findProductsByTypeInBatch(productType.getId(), batchNum, batchSize);
      alarmService.makeAllTypeDeleteAlarm(productBatch.getContent());
      batchNum++;
    } while (productBatch.hasNext());
    entityManagerService.flush();
    productManageService.changeProductTypeToBaseType(baseProductType, productTypeId);

    productType =
        productTypeService
            .findById(productTypeId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 ProductType이 존재하지 않습니다."));
    productTypeRepository.delete(productType);
  }
}
