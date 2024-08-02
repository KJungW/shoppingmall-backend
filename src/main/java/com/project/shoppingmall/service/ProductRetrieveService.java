package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.InvalidEnumType;
import com.project.shoppingmall.repository.ProductRetrieveRepository;
import com.project.shoppingmall.type.ProductRetrieveFilterType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductRetrieveService {
  private final ProductRetrieveRepository productRetrieveRepository;

  public Slice<Product> retrieveByTypeWithFilter(
      Long productTypeId, int sliceSize, int sliceNum, ProductRetrieveFilterType filterType) {
    PageRequest pageRequest = makePageRequest(sliceSize, sliceNum, filterType);
    return productRetrieveRepository.findByProductTypeId(productTypeId, pageRequest);
  }

  public Slice<Product> retrieveBySearchWordWithFilter(
      String searchWord, int sliceSize, int sliceNum, ProductRetrieveFilterType filterType) {
    PageRequest pageRequest = makePageRequest(sliceSize, sliceNum, filterType);
    return productRetrieveRepository.findByNameContainingIgnoreCase(searchWord, pageRequest);
  }

  private PageRequest makePageRequest(
      int sliceSize, int sliceNum, ProductRetrieveFilterType filterType) {
    PageRequest pageRequest = null;
    switch (filterType) {
      case LATEST:
        pageRequest =
            PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
        break;
      case OLDEST:
        pageRequest =
            PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.ASC, "createDate"));
        break;
      case LOW_PRICE:
        pageRequest =
            PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.ASC, "finalPrice"));
        break;
      case HIGH_PRICE:
        pageRequest =
            PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.DESC, "finalPrice"));
        break;
      case LOW_SCORE:
        pageRequest = PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.ASC, "scoreAvg"));
        break;
      case HIGH_SCORE:
        pageRequest = PageRequest.of(sliceNum, sliceSize, Sort.by(Sort.Direction.DESC, "scoreAvg"));
        break;
      default:
        throw new InvalidEnumType("유효하지 않은 ProductRetrieveFilterType 타입입니다.");
    }
    return pageRequest;
  }
}
