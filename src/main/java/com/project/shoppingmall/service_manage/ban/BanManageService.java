package com.project.shoppingmall.service_manage.ban;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.EntityManagerService;
import com.project.shoppingmall.service.alarm.AlarmService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.service_manage.product.ProductManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BanManageService {
  private final MemberFindService memberFindService;
  private final ProductFindService productFindService;
  private final ProductManageService productManageService;
  private final ReviewService reviewService;
  private final AlarmService alarmService;
  private final EntityManagerService entityManagerService;

  @Transactional
  public Member banMember(long memberId, boolean isBan) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원 데이터가 존재하지 않습니다."));
    if (member.getIsBan().equals(isBan)) return member;

    member.updateMemberBan(isBan);
    alarmService.makeMemberBanAlarm(member.getId());
    entityManagerService.flush();

    productManageService.banProductsBySellerId(memberId, isBan);
    reviewService.banReviewsByWriterId(memberId, isBan);
    return member;
  }

  @Transactional
  public Product banProduct(long productId, boolean isBan) {
    Product product =
        productFindService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품 데이터가 존재하지 않습니다."));
    product.updateIsBan(isBan);
    alarmService.makeProductBanAlarm(product.getId());
    return product;
  }

  @Transactional
  public Review banReview(long reviewId, boolean isBan) {
    Review review =
        reviewService
            .findById(reviewId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 리뷰 데이터가 존재하지 않습니다."));
    review.updateIsBan(isBan);
    alarmService.makeReviewBanAlarm(review.getId());
    return review;
  }
}
