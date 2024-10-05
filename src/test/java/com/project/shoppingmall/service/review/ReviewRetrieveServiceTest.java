package com.project.shoppingmall.service.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.review.ReviewDto;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.repository.ReviewRetrieveRepository;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_dto.SliceResultManager;
import com.project.shoppingmall.test_dto.review.ReviewDtoManager;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import com.project.shoppingmall.type.LoginType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

class ReviewRetrieveServiceTest {
  private ReviewRetrieveService target;
  private ReviewRetrieveRepository mockReviewRetrieveRepository;
  private ProductFindService mockProductFindService;

  @BeforeEach
  public void beforeEach() {
    this.mockReviewRetrieveRepository = mock(ReviewRetrieveRepository.class);
    this.mockProductFindService = mock(ProductFindService.class);
    target = new ReviewRetrieveService(mockReviewRetrieveRepository, mockProductFindService);
  }

  @Test
  @DisplayName("retrieveByProduct() : 정상흐름")
  public void retrieveByProduct_ok() {
    // given
    long inputProductId = 10L;
    int inputSliceNum = 3;
    int inputSliceSize = 20;

    Member givenReviewer = MemberBuilder.makeMember(50L, LoginType.NAVER);
    Member givenSeller = MemberBuilder.makeMember(40L, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller);
    List<Review> givenReviews = setReviews(List.of(10L, 20L, 30L), givenReviewer, givenProduct);
    Slice<Review> givenSlice =
        SliceManager.setMockSlice(inputSliceNum, inputSliceSize, givenReviews);

    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));
    when(mockReviewRetrieveRepository.findAllByProduct(anyLong(), any())).thenReturn(givenSlice);

    // when
    SliceResult<ReviewDto> result =
        target.retrieveByProduct(inputProductId, inputSliceNum, inputSliceSize);

    // then
    check_reviewRetrieveRepository_findAllByProduct(inputProductId, inputSliceNum, inputSliceSize);
    SliceResultManager.checkOnlySliceData(givenSlice, result);
    ReviewDtoManager.checkList(givenReviews, result.getContentList());
  }

  public List<Review> setReviews(List<Long> idList, Member givenReviewer, Product givenProduct) {
    ArrayList<Review> reviews = new ArrayList<>();
    idList.forEach(
        id -> {
          reviews.add(ReviewBuilder.makeReview(734L, givenReviewer, givenProduct));
        });
    return reviews;
  }

  public void check_reviewRetrieveRepository_findAllByProduct(
      long givenProductId, long givenSliceNum, long givenSliceSize) {
    ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockReviewRetrieveRepository, times(1))
        .findAllByProduct(productIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductId, productIdCaptor.getValue());
    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }
}
