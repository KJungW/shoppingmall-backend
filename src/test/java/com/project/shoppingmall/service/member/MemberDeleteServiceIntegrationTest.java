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
import org.jetbrains.annotations.NotNull;
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
class MemberDeleteServiceIntegrationTest {
  @Autowired private MemberDeleteService target;

  @Autowired private EntityManager em;

  @Autowired private MongoTemplate mongoTemplate;

  private Long givenMemberId;

  @BeforeEach
  public void beforeEach() throws IOException {

    ProductType givenType = saveProductType("test$test");
    Member otherMember = saveMember();
    Product otherMemberProduct = saveProduct(otherMember, givenType);

    Member targetMember = saveMember();
    givenMemberId = targetMember.getId();

    PurchaseItem targetPurchaseItem = savePurchaseItem(otherMemberProduct, targetMember);
    Review targetReview = saveReview(targetMember, otherMemberProduct, targetPurchaseItem);
    BasketItem targetBasketItem = saveBasketItem(targetMember, otherMemberProduct);
    Product targetProduct = saveProduct(targetMember, givenType);
    Alarm targetAlarm = saveAlarm(targetMember);

    PurchaseItem otherPurchaseItem = savePurchaseItem(targetProduct, otherMember);
    Review otherReview = saveReview(otherMember, targetProduct, otherPurchaseItem);

    ProductReport targetProductReport = saveProductReport(targetMember, otherMemberProduct);
    ReviewReport targetReviewReport = saveReviewReport(targetMember, otherReview);

    ChatRoom targetChatBuyerRoom = saveChatRoom(targetMember, otherMemberProduct);
    ChatRoom targetSellerChatRoom = saveChatRoom(otherMember, targetProduct);
    ChatMessage targetChatMessageInBuyerChatRoom =
        saveChatMessage(targetChatBuyerRoom, targetMember, "test message");
    ChatMessage targetChatMessageByInSellerChatRoom =
        saveChatMessage(targetSellerChatRoom, targetMember, "test message");

    ChatReadRecord targetReadRecordByBuyer = saveChatReadRecord(targetChatBuyerRoom, targetMember);
    ChatReadRecord targetReadRecordBySeller =
        saveChatReadRecord(targetSellerChatRoom, targetMember);

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
  }

  private ChatReadRecord saveChatReadRecord(ChatRoom chatRoom, Member member) {
    ChatReadRecord chatReadRecord = ChatReadRecordBuilder.makeChatReadRecord(chatRoom, member);
    em.persist(chatReadRecord);
    return chatReadRecord;
  }

  private ChatMessage saveChatMessage(ChatRoom chatRoom, Member writer, String message) {
    ChatMessage chatMessage = ChatMessageBuilder.makeChatMessage(chatRoom, writer, message);
    mongoTemplate.insert(chatMessage);
    return chatMessage;
  }

  private ChatRoom saveChatRoom(Member buyer, Product product) {
    ChatRoom chatRoom = ChatRoomBuilder.makeChatRoom(buyer, product);
    em.persist(chatRoom);
    return chatRoom;
  }

  private ReviewReport saveReviewReport(Member reporter, Review review) throws IOException {
    ReviewReport reviewReport = ReviewReportBuilder.makeProcessedReviewReport(reporter, review);
    em.persist(reviewReport);
    return reviewReport;
  }

  private ProductReport saveProductReport(Member reporter, Product product) throws IOException {
    ProductReport productReport =
        ProductReportBuilder.makeNoProcessedProductReport(reporter, product);
    em.persist(productReport);
    return productReport;
  }

  private Alarm saveAlarm(Member listener) throws IOException {
    Alarm alarm = AlarmBuilder.makeMemberBanAlarm(listener);
    em.persist(alarm);
    return alarm;
  }

  private BasketItem saveBasketItem(Member member, Product product) throws IOException {
    BasketItem basketItem = BasketItemBuilder.makeBasketItem(member, product);
    em.persist(basketItem);
    return basketItem;
  }

  private Review saveReview(Member reviewer, Product product, PurchaseItem purchaseItem)
      throws IOException {
    Review review = ReviewBuilder.makeReview(reviewer, product);
    purchaseItem.registerReview(review);
    em.persist(review);
    return review;
  }

  private PurchaseItem savePurchaseItem(Product product, Member buyer) throws IOException {
    PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(product);
    Purchase purchase =
        PurchaseBuilder.makeCompleteStatePurchase(buyer, new ArrayList<>(List.of(purchaseItem)));
    em.persist(purchase);
    return purchaseItem;
  }

  private Product saveProduct(Member seller, ProductType type) throws IOException {
    Product product = ProductBuilder.makeNoBannedProduct(seller, type);
    em.persist(product);
    return product;
  }

  private Member saveMember() {
    Member otherMember = MemberBuilder.fullData().build();
    em.persist(otherMember);
    return otherMember;
  }

  @NotNull
  private ProductType saveProductType(String typeName) {
    ProductType givenType = new ProductType(typeName);
    em.persist(givenType);
    return givenType;
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
}
