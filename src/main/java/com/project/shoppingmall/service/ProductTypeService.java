package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.repository.ProductTypeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeService {
  private final ProductTypeRepository productTypeRepository;

  public ProductType save(String typeName) {
    return productTypeRepository.save(new ProductType(typeName));
  }

  public Optional<ProductType> findById(Long typeId) {
    return productTypeRepository.findById(typeId);
  }

  public List<ProductType> getAllProductType() {
    return productTypeRepository.findAll();
  }
}
