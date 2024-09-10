package com.project.shoppingmall.service.product;

import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.repository.ProductRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductFindService {
  private final ProductRepository productRepository;

  public Optional<Product> findById(Long productId) {
    return productRepository.findById(productId);
  }

  public Optional<Product> findByIdWithSeller(Long productId) {
    return productRepository.findByIdWithSeller(productId);
  }

  public Optional<Product> findByIdWithAll(Long productId) {
    Optional<Product> result = productRepository.findByIdWithAll(productId);
    if (result.isPresent()) {
      Product product = result.get();
      int productImageSize = product.getProductImages().size();
      int singleOptionSize = product.getSingleOptions().size();
      int multiOptionSize = product.getMultipleOptions().size();
      return Optional.of(product);
    } else {
      return Optional.empty();
    }
  }
}
