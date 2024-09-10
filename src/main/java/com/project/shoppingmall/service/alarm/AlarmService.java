package com.project.shoppingmall.service.alarm;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.AlarmRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.refund.RefundFindService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.type.AlarmType;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmService {
  private final AlarmRepository alarmRepository;
  private final MemberFindService memberFindService;
  private final ReviewService reviewService;
  private final ProductService productService;
  private final RefundFindService refundFindService;

  @Transactional
  public Alarm makeMemberBanAlarm(long listenerId) {
    Member listener =
        memberFindService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));

    Alarm memberBanAlarm =
        Alarm.builder().listener(listener).alarmType(AlarmType.MEMBER_BAN).build();

    alarmRepository.save(memberBanAlarm);
    return memberBanAlarm;
  }

  @Transactional
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

  @Transactional
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

  @Transactional
  public Alarm makeRefundRequestAlarm(long refundId) {
    Refund refund =
        refundFindService
            .findByIdWithPurchaseItemProduct(refundId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));
    Member listener =
        memberFindService
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

  @Transactional
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

  @Transactional
  public List<Alarm> makeAllTypeDeleteAlarm(List<Product> products) {
    List<Alarm> newAlarms = new ArrayList<>();
    products.forEach(
        product -> {
          newAlarms.add(
              Alarm.builder()
                  .listener(product.getSeller())
                  .alarmType(AlarmType.TYPE_DELETE)
                  .targetProduct(product)
                  .build());
        });

    try {
      alarmRepository.saveAll(newAlarms);
    } catch (Exception ex) {
      throw new ServerLogicError("product들 중에 DB에 존재하지 않는 product가 존재합니다.");
    }

    return newAlarms;
  }

  @Transactional
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

  @Transactional
  public List<Alarm> makeAllTypeUpdateAlarm(List<Product> products) {
    List<Alarm> newAlarms = new ArrayList<>();
    products.forEach(
        product -> {
          newAlarms.add(
              Alarm.builder()
                  .listener(product.getSeller())
                  .alarmType(AlarmType.TYPE_UPDATE)
                  .targetProduct(product)
                  .build());
        });

    try {
      alarmRepository.saveAll(newAlarms);
    } catch (Exception ex) {
      throw new ServerLogicError("product들 중에 DB에 존재하지 않는 product가 존재합니다.");
    }

    return newAlarms;
  }
}
