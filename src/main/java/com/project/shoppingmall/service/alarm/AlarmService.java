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

  public Alarm makeReviewBanAlarm(long reviewId) {
    Review review =
        reviewService
            .findByIdWithWriter(reviewId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 리뷰가 존재하지 않습니다."));

    Alarm reviewBanAlarm =
        Alarm.builder()
            .listener(review.getWriter())
            .alarmType(AlarmType.REVIEW_BAN)
            .targetReview(review)
            .build();

    alarmRepository.save(reviewBanAlarm);
    return reviewBanAlarm;
  }

  public Alarm makeProductBanAlarm(long productId) {
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    Alarm productBanAlarm =
        Alarm.builder()
            .listener(product.getSeller())
            .alarmType(AlarmType.PRODUCT_BAN)
            .targetProduct(product)
            .build();

    alarmRepository.save(productBanAlarm);
    return productBanAlarm;
  }

  public Alarm makeRefundRequestAlarm(long refundId) {
    Refund refund =
        refundFindService
            .findByIdWithPurchaseItemProduct(refundId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));
    Member listener =
        memberService
            .findById(refund.getPurchaseItem().getSellerId())
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));

    Alarm refundRequestAlarm =
        Alarm.builder()
            .listener(listener)
            .alarmType(AlarmType.REFUND_REQUEST)
            .targetRefund(refund)
            .build();

    alarmRepository.save(refundRequestAlarm);
    return refundRequestAlarm;
  }

  public Alarm makeTypeDeleteAlarm(long productId) {
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    Alarm typeDeleteAlarm =
        Alarm.builder()
            .listener(product.getSeller())
            .alarmType(AlarmType.TYPE_DELETE)
            .targetProduct(product)
            .build();

    alarmRepository.save(typeDeleteAlarm);
    return typeDeleteAlarm;
  }

  public Alarm makeTypeUpdateAlarm(long productId) {
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    Alarm typeUpdateAlarm =
        Alarm.builder()
            .listener(product.getSeller())
            .alarmType(AlarmType.TYPE_UPDATE)
            .targetProduct(product)
            .build();

    alarmRepository.save(typeUpdateAlarm);
    return typeUpdateAlarm;
  }
}
