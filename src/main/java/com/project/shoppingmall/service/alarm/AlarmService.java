package com.project.shoppingmall.service.alarm;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.AlarmRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.refund.RefundFindService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.type.AlarmType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmService {
  private final AlarmRepository alarmRepository;
  private final MemberService memberService;
  private final ReviewService reviewService;
  private final ProductService productService;
  private final RefundFindService refundFindService;

  public Alarm makeMemberBanAlarm(long listenerId) {
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));

    Alarm memberBanAlarm =
        Alarm.builder().listener(listener).alarmType(AlarmType.MEMBER_BAN).build();

    alarmRepository.save(memberBanAlarm);
    return memberBanAlarm;
  }

  public Alarm makeReviewBanAlarm(long listenerId, long reviewId) {
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Review review =
        reviewService
            .findByIdWithWriter(reviewId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 리뷰가 존재하지 않습니다."));

    if (!review.getWriter().getId().equals(listener.getId()))
      throw new DataNotFound("현재 리뷰는 현재 회원이 작성한 리뷰가 아닙니다.");

    Alarm reviewBanAlarm =
        Alarm.builder()
            .listener(listener)
            .alarmType(AlarmType.REVIEW_BAN)
            .targetReview(review)
            .build();

    alarmRepository.save(reviewBanAlarm);
    return reviewBanAlarm;
  }

  public Alarm makeProductBanAlarm(long listenerId, long productId) {
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (!product.getSeller().getId().equals(listener.getId()))
      throw new DataNotFound("현재 제품은 현재 회원이 판매중인 제품이 아닙니다.");

    Alarm productBanAlarm =
        Alarm.builder()
            .listener(listener)
            .alarmType(AlarmType.PRODUCT_BAN)
            .targetProduct(product)
            .build();

    alarmRepository.save(productBanAlarm);
    return productBanAlarm;
  }

  public Alarm makeRefundRequestAlarm(long listenerId, long refundId) {
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Refund refund =
        refundFindService
            .findByIdWithPurchaseItemProduct(refundId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (!refund.getPurchaseItem().getSellerId().equals(listener.getId()))
      throw new DataNotFound("현재 환불은 다른 회원의 판매상품에 대한 환불입니다.");

    Alarm refundRequestAlarm =
        Alarm.builder()
            .listener(listener)
            .alarmType(AlarmType.REFUND_REQUEST)
            .targetRefund(refund)
            .build();

    alarmRepository.save(refundRequestAlarm);
    return refundRequestAlarm;
  }

  public Alarm makeTypeDeleteAlarm(long listenerId, long productId) {
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (!product.getSeller().getId().equals(listener.getId()))
      throw new DataNotFound("현재 제품은 현재 회원이 판매중인 제품이 아닙니다.");

    Alarm typeDeleteAlarm =
        Alarm.builder()
            .listener(listener)
            .alarmType(AlarmType.TYPE_DELETE)
            .targetProduct(product)
            .build();

    alarmRepository.save(typeDeleteAlarm);
    return typeDeleteAlarm;
  }

  public Alarm makeTypeUpdateAlarm(long listenerId, long productId) {
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (!product.getSeller().getId().equals(listener.getId()))
      throw new DataNotFound("현재 제품은 현재 회원이 판매중인 제품이 아닙니다.");

    Alarm typeUpdateAlarm =
        Alarm.builder()
            .listener(listener)
            .alarmType(AlarmType.TYPE_UPDATE)
            .targetProduct(product)
            .build();

    alarmRepository.save(typeUpdateAlarm);
    return typeUpdateAlarm;
  }
}
