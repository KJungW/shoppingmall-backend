package com.project.shoppingmall.service.review;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.review.ReviewDto;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ReviewRetrieveRepository;
import com.project.shoppingmall.service.product.ProductFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewRetrieveService {
  private final ReviewRetrieveRepository reviewRetrieveRepository;
  private final ProductFindService productFindService;

  public SliceResult<ReviewDto> retrieveByProduct(long productId, int sliceNumber, int sliceSize) {
    Product product =
        productFindService
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<Review> sliceResult =
        reviewRetrieveRepository.findAllByProduct(product.getId(), pageRequest);
    List<ReviewDto> contents = sliceResult.getContent().stream().map(ReviewDto::new).toList();
    return new SliceResult<ReviewDto>(sliceResult, contents);
  }
}
