package com.project.shoppingmall.service.member;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.testdata.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class MemberDeleteServiceTest {
  @Autowired private MemberDeleteService target;

  @Autowired private EntityManager em;

  @Autowired private MongoTemplate mongoTemplate;

  private Long givenMemberId;

  @BeforeEach
  public void beforeEach() throws IOException {

    ProductType givenType = new ProductType("test$test");
    em.persist(givenType);

    Member otherMember = MemberBuilder.fullData().build();
    em.persist(otherMember);
    Product otherMemberProduct = ProductBuilder.makeNoBannedProduct(otherMember, givenType);
    em.persist(otherMemberProduct);

    // 타켓 회원 생성
    Member deletedMember = MemberBuilder.fullData().build();
    em.persist(deletedMember);
    givenMemberId = deletedMember.getId();

    // 타켓 회원의 구매와 구매아이템 생성
    PurchaseItem targetPurchaseItem = PurchaseItemBuilder.makePurchaseItem(otherMemberProduct);
    Purchase targetPurchase =
        PurchaseBuilder.makeCompleteStatePurchase(
            deletedMember, new ArrayList<>(List.of(targetPurchaseItem)));
    em.persist(targetPurchase);

    // 타켓 회원의 리뷰 생성
    Review targetReview = ReviewBuilder.makeReview(deletedMember, otherMemberProduct);
    targetPurchaseItem.registerReview(targetReview);
    em.persist(targetReview);

    // 타켓 회원의 장바구니 생성
    BasketItem targetBasketItem =
        BasketItemBuilder.makeBasketItem(deletedMember, otherMemberProduct);
    em.persist(targetBasketItem);

    // 타겟 회원의 제품 생성
    Product targetProduct = ProductBuilder.makeNoBannedProduct(deletedMember, givenType);
    em.persist(targetProduct);

    // 타겟 회원의 알림 생성
    Alarm targetAlarm = AlarmBuilder.makeMemberBanAlarm(deletedMember);
    em.persist(targetAlarm);

    // 다른 회원의 리뷰 생성
    PurchaseItem otherPurchaseItem = PurchaseItemBuilder.makePurchaseItem(targetProduct);
    Purchase otherPurchase =
        PurchaseBuilder.makeCompleteStatePurchase(
            otherMember, new ArrayList<>(List.of(otherPurchaseItem)));
    em.persist(otherPurchase);
    Review otherReview = ReviewBuilder.makeReview(otherMember, targetProduct);
    otherPurchaseItem.registerReview(otherReview);
    em.persist(otherReview);

    // 타켓 회원의 신고 생성
    ProductReport targetProductReport =
        ProductReportBuilder.makeNoProcessedProductReport(deletedMember, otherMemberProduct);
    em.persist(targetProductReport);
    ReviewReport targetReviewReport =
        ReviewReportBuilder.makeProcessedReviewReport(deletedMember, otherReview);
    em.persist(targetReviewReport);

    // 타켓 회원의 채팅방 생성
    ChatRoom targetChatBuyerRoom = ChatRoomBuilder.makeChatRoom(deletedMember, otherMemberProduct);
    em.persist(targetChatBuyerRoom);
    ChatRoom targetSellerChatRoom = ChatRoomBuilder.makeChatRoom(otherMember, targetProduct);
    em.persist(targetSellerChatRoom);

    // 타겟 회원의 채팅 메세지 생성
    ChatMessage targetChatMessageInBuyerChatRoom =
        ChatMessageBuilder.makeChatMessage(targetChatBuyerRoom, deletedMember, "test message");
    mongoTemplate.insert(targetChatMessageInBuyerChatRoom);
    ChatMessage targetChatMessageByInSellerChatRoom =
        ChatMessageBuilder.makeChatMessage(targetSellerChatRoom, deletedMember, "test message");
    mongoTemplate.insert(targetChatMessageByInSellerChatRoom);

    // 타겟 회원의 읽기 기록 생성
    ChatReadRecord targetReadRecordByBuyer =
        ChatReadRecordBuilder.makeChatReadRecord(targetChatBuyerRoom, deletedMember);
    em.persist(targetReadRecordByBuyer);
    ChatReadRecord targetReadRecordBySeller =
        ChatReadRecordBuilder.makeChatReadRecord(targetSellerChatRoom, deletedMember);
    em.persist(targetReadRecordBySeller);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("deleteMember : 정상흐름")
  public void deleteMember() {
    // given
    Member givenMember =
        em.createQuery("select m from Member m where m.id = :memberId", Member.class)
            .setParameter("memberId", givenMemberId)
            .getSingleResult();

    // when
    target.deleteMember(givenMember);

    // then
    // - 타켓 회원의 구매와 구매아이템은 제거되지 않음
    Purchase realPurchase =
        em.createQuery("select p from Purchase p where p.buyerId = :buyerId", Purchase.class)
            .setParameter("buyerId", givenMemberId)
            .getSingleResult();
    PurchaseItem realPurchaseItem =
        em.createQuery(
                "select pi from PurchaseItem pi where pi.purchase.id = :purchaseId",
                PurchaseItem.class)
            .setParameter("purchaseId", realPurchase.getId())
            .getSingleResult();

    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select r from Review r where r.writer.id = :writerId", Review.class)
                .setParameter("writerId", givenMemberId)
                .getSingleResult());

    // - 타겟 회원의 리뷰 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select r from Review r where r.writer.id = :writerId", Review.class)
                .setParameter("writerId", givenMemberId)
                .getSingleResult());

    // - 타겟 회원의 장바구니 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select bi from BasketItem bi where bi.member.id = :memberId", BasketItem.class)
                .setParameter("memberId", givenMemberId)
                .getSingleResult());

    // - 타겟 회원의 판매 제품 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select p from Product p where p.seller.id = :sellerId", Product.class)
                .setParameter("sellerId", givenMemberId)
                .getSingleResult());

    // - 타겟 회원의 알림 제품 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select a from Alarm a where a.listener.id = :listenerId", Alarm.class)
                .setParameter("listenerId", givenMemberId)
                .getSingleResult());

    // - 타겟 회원의 신고 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select pr from ProductReport pr where pr.reporter.id = :reporterId",
                    ProductReport.class)
                .setParameter("reporterId", givenMemberId)
                .getSingleResult());
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select rr from ReviewReport rr where rr.reporter.id = :reporterId",
                    ReviewReport.class)
                .setParameter("reporterId", givenMemberId)
                .getSingleResult());

    // - 타켓 회원이 참여중인 채팅방 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select cr from ChatRoom cr where cr.buyer.id = :buyerId", ChatRoom.class)
                .setParameter("buyerId", givenMemberId)
                .getSingleResult());
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select cr from ChatRoom cr where cr.seller.id = :sellerId", ChatRoom.class)
                .setParameter("sellerId", givenMemberId)
                .getSingleResult());

    // - 타겟 회원의 채팅메세지 제거 확인
    Query startChatMessageQuery = new Query();
    startChatMessageQuery.addCriteria(Criteria.where("writerId").is(givenMemberId));
    List<ChatMessage> targetMemberChatMessages =
        mongoTemplate.find(startChatMessageQuery, ChatMessage.class);
    assertEquals(0, targetMemberChatMessages.size());

    // - 타겟 회원의 읽기 기록 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select crr from ChatReadRecord crr where crr.member.id = :memberId",
                    ChatReadRecord.class)
                .setParameter("memberId", givenMemberId)
                .getSingleResult());

    // - 타켓 회원 제거 확인
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select m from Member m where m.id = :memberId", Member.class)
                .setParameter("memberId", givenMemberId)
                .getSingleResult());
  }
}
