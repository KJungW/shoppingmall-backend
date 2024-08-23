package com.project.shoppingmall.service_manage.ban;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BanManageService {
  private final MemberService memberService;
  private final ProductService productService;
  private final ReviewService reviewService;

  @Transactional
  public Member banMember(long memberId, boolean isBan) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원 데이터가 존재하지 않습니다."));
    member.updateMemberBan(isBan);
    return member;
  }

  @Transactional
  public Product banProduct(long productId, boolean isBan) {
    Product product =
        productService
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품 데이터가 존재하지 않습니다."));
    product.updateIsBan(isBan);
    return product;
  }

  @Transactional
  public Review banReview(long reviewId, boolean isBan) {
    Review review =
        reviewService
            .findById(reviewId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 리뷰 데이터가 존재하지 않습니다."));
    review.updateIsBan(isBan);
    return review;
  }
}
