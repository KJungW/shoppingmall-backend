package com.project.shoppingmall.service.product;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.InvalidEnumType;
import com.project.shoppingmall.repository.ProductRetrieveRepository;
import com.project.shoppingmall.service.member.MemberService;
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
  private final MemberService memberService;

  public Slice<Product> retrieveByTypeWithFilter(
      Long productTypeId, int sliceSize, int sliceNum, ProductRetrieveFilterType filterType) {
    PageRequest pageRequest = makePageRequest(sliceSize, sliceNum, filterType);
    Slice<Product> sliceResult =
        productRetrieveRepository.findByProductType(productTypeId, pageRequest);
    sliceResult.getContent().forEach(product -> product.getProductImages().get(0));
    return sliceResult;
  }

  public Slice<Product> retrieveBySearchWordWithFilter(
      String searchWord, int sliceSize, int sliceNum, ProductRetrieveFilterType filterType) {
    PageRequest pageRequest = makePageRequest(sliceSize, sliceNum, filterType);
    Slice<Product> sliceResult =
        productRetrieveRepository.findBySearchWord(searchWord, pageRequest);
    sliceResult.getContent().forEach(product -> product.getProductImages().get(0));
    return sliceResult;
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

  public Slice<Product> retrieveBySeller(long sellerId, int sliceNumber, int sliceSize) {
    Member seller =
        memberService
            .findById(sellerId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<Product> sliceResult =
        productRetrieveRepository.findAllBySeller(seller.getId(), pageRequest);
    sliceResult.getContent().forEach(product -> product.getProductImages().get(0));
    return sliceResult;
  }

  public Slice<Product> retrieveByRandom(Integer sliceNumber, Integer sliceSize) {
    PageRequest pageRequest = PageRequest.of(sliceNumber, sliceSize);
    Slice<Product> sliceResult = productRetrieveRepository.findAllByRandom(pageRequest);
    sliceResult.getContent().forEach(product -> product.getProductImages().get(0));
    return sliceResult;
  }
}
