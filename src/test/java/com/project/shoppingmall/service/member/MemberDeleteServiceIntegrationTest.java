package com.project.shoppingmall.service.member;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.test_entity.*;
import com.project.shoppingmall.type.ReportResultType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@SpringBootTest
@Transactional
@Rollback
class MemberDeleteServiceIntegrationTest {
  @Autowired private MemberDeleteService target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;
  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  private Long givenMemberId;

  @BeforeEach
  public void beforeEach() throws IOException {
    Member targetMember = testDataMaker.saveMember();
    testDataMaker.saveMemberProfileImage(targetMember);
    givenMemberId = targetMember.getId();
    Member otherMember = testDataMaker.saveMember();

    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product targetProduct = testDataMaker.saveProduct(targetMember, givenType);
    Product otherMemberProduct = testDataMaker.saveProduct(otherMember, givenType);

    PurchaseItem targetPurchaseItem =
        testDataMaker.savePurchaseItem(otherMemberProduct, targetMember);

    Review targetReview =
        testDataMaker.saveReview(targetMember, otherMemberProduct, targetPurchaseItem);
    BasketItem targetBasketItem = testDataMaker.saveBasketItem(targetMember, otherMemberProduct);
    Alarm targetAlarm = testDataMaker.saveMemberBanAlarm(targetMember);

    PurchaseItem otherPurchaseItem = testDataMaker.savePurchaseItem(targetProduct, otherMember);
    Review otherReview = testDataMaker.saveReview(otherMember, targetProduct, otherPurchaseItem);

    ProductReport targetProductReport =
        testDataMaker.saveProductReport(
            targetMember, otherMemberProduct, ReportResultType.WAITING_PROCESSED);
    ReviewReport targetReviewReport =
        testDataMaker.saveReviewReport(targetMember, otherReview, ReportResultType.NO_ACTION);

    ChatRoom targetChatBuyerRoom = testDataMaker.saveChatRoom(targetMember, otherMemberProduct);
    ChatRoom targetSellerChatRoom = testDataMaker.saveChatRoom(otherMember, targetProduct);
    ChatMessage targetChatMessageInBuyerChatRoom =
        testDataMaker.saveChatMessage(targetChatBuyerRoom, targetMember, "test message");
    ChatMessage targetChatMessageByInSellerChatRoom =
        testDataMaker.saveChatMessage(targetSellerChatRoom, targetMember, "test message");
    ChatReadRecord targetReadRecordByBuyer =
        testDataMaker.saveChatReadRecord(targetChatBuyerRoom, targetMember);
    ChatReadRecord targetReadRecordBySeller =
        testDataMaker.saveChatReadRecord(targetSellerChatRoom, targetMember);

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
    em.flush();
    em.clear();

    // then
    checkPurchaseAndPurchaseItemIsNotDelete();
    checkReviewIsDelete();
    checkBasketItemIsDelete();
    checkProductIsDelete();
    checkAlarmIsDelete();
    checkReportIsDelete();
    checkChatRoomIsDelete();
    checkChatMessageIsDelete();
    checkChatReadRecordIsDelete();
    checkMemberIsDelete();
    checkMemberProfileImageDelete(givenMember.getProfileImageUrl());
  }

  private void checkMemberIsDelete() {
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select m from Member m where m.id = :memberId", Member.class)
                .setParameter("memberId", givenMemberId)
                .getSingleResult());
  }

  private void checkChatReadRecordIsDelete() {
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select crr from ChatReadRecord crr where crr.member.id = :memberId",
                    ChatReadRecord.class)
                .setParameter("memberId", givenMemberId)
                .getSingleResult());
  }

  private void checkChatMessageIsDelete() {
    Query startChatMessageQuery = new Query();
    startChatMessageQuery.addCriteria(Criteria.where("writerId").is(givenMemberId));
    List<ChatMessage> targetMemberChatMessages =
        mongoTemplate.find(startChatMessageQuery, ChatMessage.class);
    assertEquals(0, targetMemberChatMessages.size());
  }

  private void checkChatRoomIsDelete() {
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
  }

  private void checkReportIsDelete() {
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
  }

  private void checkAlarmIsDelete() {
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select a from Alarm a where a.listener.id = :listenerId", Alarm.class)
                .setParameter("listenerId", givenMemberId)
                .getSingleResult());
  }

  private void checkProductIsDelete() {
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select p from Product p where p.seller.id = :sellerId", Product.class)
                .setParameter("sellerId", givenMemberId)
                .getSingleResult());
  }

  private void checkBasketItemIsDelete() {
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery(
                    "select bi from BasketItem bi where bi.member.id = :memberId", BasketItem.class)
                .setParameter("memberId", givenMemberId)
                .getSingleResult());
  }

  private void checkReviewIsDelete() {
    assertThrows(
        NoResultException.class,
        () ->
            em.createQuery("select r from Review r where r.writer.id = :writerId", Review.class)
                .setParameter("writerId", givenMemberId)
                .getSingleResult());
  }

  public void checkPurchaseAndPurchaseItemIsNotDelete() {
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
  }

  public void checkMemberProfileImageDelete(String givenImageUri) {
    assertThrows(
        NoSuchKeyException.class,
        () -> {
          HeadObjectRequest headObjectRequest =
              HeadObjectRequest.builder().bucket(bucketName).key(givenImageUri).build();
          s3Client.headObject(headObjectRequest);
        });
  }
}
