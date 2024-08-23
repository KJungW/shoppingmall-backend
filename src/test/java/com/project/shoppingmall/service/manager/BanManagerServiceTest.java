package com.project.shoppingmall.service.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import com.project.shoppingmall.testdata.ReviewBuilder;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class BanManagerServiceTest {
  private BanManagerService target;
  private MemberService mockMemberService;
  private ProductService mockProductService;
  private ReviewService mockReviewService;

  @BeforeEach
  public void beforeEach() {
    mockMemberService = mock(MemberService.class);
    mockProductService = mock(ProductService.class);
    mockReviewService = mock(ReviewService.class);
    target = new BanManagerService(mockMemberService, mockProductService, mockReviewService);
  }

  @Test
  @DisplayName("banMember() : 정상흐름")
  public void banMember_ok() {
    // given
    long givenMemberId = 20L;
    boolean givenIsBan = true;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    ReflectionTestUtils.setField(givenMember, "isBan", !givenIsBan);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    target.banMember(givenMemberId, givenIsBan);

    // then
    assertEquals(givenIsBan, givenMember.getIsBan());
  }

  @Test
  @DisplayName("banMember() : 조회된 회원이 존재하지 않음")
  public void banMember_noMember() {
    // given
    long givenMemberId = 20L;
    boolean givenIsBan = true;

    when(mockMemberService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.banMember(givenMemberId, givenIsBan));
  }

  @Test
  @DisplayName("banProduct() : 정상흐름")
  public void banProduct_ok() throws IOException {
    // given
    long givenProductId = 20L;
    boolean givenIsBan = true;

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct, "isBan", !givenIsBan);
    when(mockProductService.findById(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    target.banProduct(givenProductId, givenIsBan);

    // then
    assertEquals(givenIsBan, givenProduct.getIsBan());
  }

  @Test
  @DisplayName("banProduct() : 조회된 제품 없음")
  public void banProduct_noProduct() throws IOException {
    // given
    long givenProductId = 20L;
    boolean givenIsBan = true;

    when(mockProductService.findById(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.banProduct(givenProductId, givenIsBan));
  }

  @Test
  @DisplayName("banReview() : 정상흐름")
  public void banReview_ok() throws IOException {
    // given
    long givenReviewId = 20L;
    boolean givenIsBan = true;

    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    ReflectionTestUtils.setField(givenReview, "isBan", !givenIsBan);
    Mockito.when(mockReviewService.findById(anyLong())).thenReturn(Optional.of(givenReview));

    // when
    target.banReview(givenReviewId, givenIsBan);

    // then
    Assertions.assertEquals(givenIsBan, givenReview.getIsBan());
  }

  @Test
  @DisplayName("banReview() : 조회된 리뷰가 없음")
  public void banReview_noReview() {
    // given
    long givenReviewId = 20L;
    boolean givenIsBan = true;

    Mockito.when(mockReviewService.findById(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.banReview(givenReviewId, givenIsBan));
  }
}
