package com.project.shoppingmall.test_entity;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.final_value.FinalValue;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.test_entity.alarm.Alarm_RealDataBuilder;
import com.project.shoppingmall.test_entity.basketitem.BasketItem_RealDataBuilder;
import com.project.shoppingmall.test_entity.chat.ChatMessage_RealDataBuilder;
import com.project.shoppingmall.test_entity.chat.ChatReadRecord_RealDataBuilder;
import com.project.shoppingmall.test_entity.chat.ChatRoom_RealDataBuilder;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.Product_RealDataBuilder;
import com.project.shoppingmall.test_entity.purchase.Purchase_RealDataBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItem_RealDataBuilder;
import com.project.shoppingmall.test_entity.refund.Refund_RealDataBuilder;
import com.project.shoppingmall.test_entity.report.ProductReport_RealDataBuilder;
import com.project.shoppingmall.test_entity.report.ReviewReport_RealDataBuilder;
import com.project.shoppingmall.test_entity.review.Review_RealDataBuilder;
import com.project.shoppingmall.type.*;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class IntegrationTestDataMaker {
  @Autowired private EntityManager em;
  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private S3Service s3Service;

  public Member saveMember() {
    Member otherMember = MemberBuilder.fullData().build();
    em.persist(otherMember);
    return otherMember;
  }

  public ProductType saveProductType(String typeName) {
    ProductType givenType = new ProductType(typeName);
    em.persist(givenType);
    return givenType;
  }

  public ProductType saveBaseProductType() {
    ProductType baseProductType = new ProductType(FinalValue.BASE_PRODUCT_TYPE_NAME);
    ReflectionTestUtils.setField(
        baseProductType,
        "typeName",
        FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME);
    em.persist(baseProductType);
    return baseProductType;
  }

  public List<ProductType> saveProductTypeList(int count) {
    return Stream.generate(() -> saveProductType("test$test" + UUID.randomUUID().toString()))
        .limit(count)
        .toList();
  }

  public Product saveProduct(Member seller, ProductType type) {
    Product product = Product_RealDataBuilder.makeProduct(seller, type);
    em.persist(product);
    return product;
  }

  public Product saveProduct(Member seller, ProductType type, boolean isBan) {
    Product product = Product_RealDataBuilder.makeProduct(seller, type, isBan);
    em.persist(product);
    return product;
  }

  public Product saveProduct(
      Member seller, ProductType type, boolean isBan, ProductSaleType saleType) {
    Product product = Product_RealDataBuilder.makeProduct(seller, type, isBan, saleType);
    em.persist(product);
    return product;
  }

  public Product saveProduct(
      String name, Member seller, ProductType type, boolean isBan, ProductSaleType saleType) {
    Product product = Product_RealDataBuilder.makeProduct(name, seller, type, isBan, saleType);
    em.persist(product);
    return product;
  }

  public Product saveProduct() {
    Member seller = saveMember();
    ProductType type = saveProductType("test$test");
    return saveProduct(seller, type);
  }

  public Product saveProduct(Member seller) {
    ProductType type = saveProductType("test$test");
    return saveProduct(seller, type);
  }

  public List<Product> saveProductList(int count, Member seller, ProductType type, boolean isBan) {
    return Stream.generate(() -> saveProduct(seller, type, isBan)).limit(count).toList();
  }

  public List<Product> saveProductList(
      int count, Member seller, ProductType type, boolean isBan, ProductSaleType saleType) {
    return Stream.generate(() -> saveProduct(seller, type, isBan, saleType)).limit(count).toList();
  }

  public List<Product> saveProductList(
      int count,
      String name,
      Member seller,
      ProductType type,
      boolean isBan,
      ProductSaleType saleType) {
    return Stream.generate(() -> saveProduct(name, seller, type, isBan, saleType))
        .limit(count)
        .toList();
  }

  public BasketItem saveBasketItem(Member member, Product product) {
    BasketItem basketItem = BasketItem_RealDataBuilder.makeBasketItem(member, product);
    em.persist(basketItem);
    return basketItem;
  }

  public Purchase savePurchase(
      Member buyer, List<PurchaseItem> purchaseItems, PurchaseStateType stateType) {
    Purchase purchase = Purchase_RealDataBuilder.makePurchase(buyer, purchaseItems, stateType);
    em.persist(purchase);
    return purchase;
  }

  public Purchase savePurchase(
      Member buyer, Product product, int purchaseItemCount, PurchaseStateType stateType) {
    List<PurchaseItem> purchaseItems =
        PurchaseItem_RealDataBuilder.makeList(purchaseItemCount, product);
    return savePurchase(buyer, purchaseItems, stateType);
  }

  public Purchase savePurchase(
      Product product, int purchaseItemCount, PurchaseStateType stateType) {
    Member givenBuyer = saveMember();
    List<PurchaseItem> purchaseItems =
        PurchaseItem_RealDataBuilder.makeList(purchaseItemCount, product);
    return savePurchase(givenBuyer, purchaseItems, stateType);
  }

  public Purchase savePurchase(Member buyer, PurchaseStateType stateType) {
    Product product = saveProduct();
    PurchaseItem purchaseItem = PurchaseItem_RealDataBuilder.make(product);
    Purchase purchase =
        Purchase_RealDataBuilder.makePurchase(buyer, List.of(purchaseItem), stateType);
    em.persist(purchase);
    return purchase;
  }

  // 삭제필요
  public PurchaseItem savePurchaseItem(Product product, Member buyer) {
    PurchaseItem purchaseItem = PurchaseItem_RealDataBuilder.make(product);
    Purchase purchase =
        Purchase_RealDataBuilder.makePurchase(
            buyer, new ArrayList<>(List.of(purchaseItem)), PurchaseStateType.COMPLETE);
    em.persist(purchase);
    return purchaseItem;
  }

  public Refund saveRefund(PurchaseItem purchaseItem, RefundStateType type) {
    Refund refund = Refund_RealDataBuilder.makeRefund(type, purchaseItem);
    em.persist(refund);
    return refund;
  }

  public Refund saveRefund(Product otherMemberProduct, RefundStateType type) {
    Member buyer = saveMember();
    Purchase purchase = savePurchase(buyer, PurchaseStateType.COMPLETE);
    return saveRefund(purchase.getPurchaseItems().get(0), type);
  }

  public List<Refund> saveRefundList(List<PurchaseItem> purchaseItems, RefundStateType type) {
    return purchaseItems.stream().map(item -> saveRefund(item, type)).toList();
  }

  public Review saveReview(Member reviewer, Product product, PurchaseItem purchaseItem) {
    Review review = Review_RealDataBuilder.makeReview(reviewer, product);
    purchaseItem.registerReview(review);
    em.persist(review);
    return review;
  }

  public Review saveReview(Member reviewer, Product product, PurchaseItem purchaseItem, int score) {
    Review review = Review_RealDataBuilder.makeReview(reviewer, product, score);
    purchaseItem.registerReview(review);
    em.persist(review);
    return review;
  }

  public Review saveReview(Member reviewer) {
    Product product = saveProduct();
    Purchase purchase = savePurchase(reviewer, PurchaseStateType.COMPLETE);
    return saveReview(reviewer, product, purchase.getPurchaseItems().get(0));
  }

  public List<Review> saveReviewList(
      Member reviewer, Product product, List<PurchaseItem> purchaseItems) {
    return purchaseItems.stream().map(item -> saveReview(reviewer, product, item)).toList();
  }

  public List<Review> saveReviewList(Product product, List<PurchaseItem> purchaseItems) {
    Member reviewer = saveMember();
    return purchaseItems.stream().map(item -> saveReview(reviewer, product, item)).toList();
  }

  public List<Review> saveReviewList(Product product, List<PurchaseItem> purchaseItems, int score) {
    Member reviewer = saveMember();
    return purchaseItems.stream().map(item -> saveReview(reviewer, product, item, score)).toList();
  }

  public ReviewReport saveReviewReport(Member reporter, Review review, ReportResultType state) {
    ReviewReport reviewReport = ReviewReport_RealDataBuilder.make(reporter, review, state);
    em.persist(reviewReport);
    return reviewReport;
  }

  public List<ReviewReport> saveReviewReportList(
      Member reporter, List<Review> reviews, ReportResultType state) {
    return reviews.stream().map(review -> saveReviewReport(reporter, review, state)).toList();
  }

  public List<ReviewReport> saveReviewReportList(List<Review> reviews, ReportResultType state) {
    Member reporter = saveMember();
    return reviews.stream().map(review -> saveReviewReport(reporter, review, state)).toList();
  }

  public ProductReport saveProductReport(Member reporter, Product product, ReportResultType state) {
    ProductReport productReport = ProductReport_RealDataBuilder.make(reporter, product, state);
    em.persist(productReport);
    return productReport;
  }

  public List<ProductReport> saveProductReportList(
      int count, Member reporter, Product product, ReportResultType state) {
    return Stream.generate(() -> saveProductReport(reporter, product, state)).limit(count).toList();
  }

  public ChatReadRecord saveChatReadRecord(ChatRoom chatRoom, Member member) {
    ChatReadRecord chatReadRecord =
        ChatReadRecord_RealDataBuilder.makeChatReadRecord(chatRoom, member);
    em.persist(chatReadRecord);
    return chatReadRecord;
  }

  public ChatMessage saveChatMessage(ChatRoom chatRoom, Member writer, String message) {
    ChatMessage chatMessage =
        ChatMessage_RealDataBuilder.makeChatMessage(chatRoom, writer, message);
    mongoTemplate.insert(chatMessage);
    return chatMessage;
  }

  public ChatRoom saveChatRoom(Member buyer, Product product) {
    ChatRoom chatRoom = ChatRoom_RealDataBuilder.makeChatRoom(buyer, product);
    em.persist(chatRoom);
    return chatRoom;
  }

  public Alarm saveMemberBanAlarm(Member listener) {
    Alarm alarm = Alarm_RealDataBuilder.makeMemberBanAlarm(listener);
    em.persist(alarm);
    return alarm;
  }

  public Alarm saveReviewBanAlarm(Member listener, Review listenerReview) {
    Alarm alarm = Alarm_RealDataBuilder.makeReviewBanAlarm(listener, listenerReview);
    em.persist(alarm);
    return alarm;
  }

  public Alarm saveProductBanAlarm(Member listener, Product listenerProduct) {
    Alarm alarm = Alarm_RealDataBuilder.makeProductBanAlarm(listener, listenerProduct);
    em.persist(alarm);
    return alarm;
  }

  public Alarm saveRefundRequestAlarm(Member listener, Refund targetRefund) {
    Alarm alarm = Alarm_RealDataBuilder.makeRefundRequestAlarm(listener, targetRefund);
    em.persist(alarm);
    return alarm;
  }

  public void saveMemberProfileImage(Member member) {
    MockMultipartFile profileImage =
        makeMultipartFile("src/test/resources/static/profileSampleImage.png");
    FileUploadResult uploadResult =
        s3Service.uploadFile(
            profileImage, "profileImg/" + member.getId() + "-" + member.getNickName() + "/");
    member.updateProfile(uploadResult.getFileServerUri(), uploadResult.getDownLoadUrl());
  }

  public void saveProductImage(Product product) {
    ArrayList<MultipartFile> testImageList =
        new ArrayList<>(
            List.of(
                makeMultipartFile("src/test/resources/static/product_image/image1.png"),
                makeMultipartFile("src/test/resources/static/product_image/image2.png"),
                makeMultipartFile("src/test/resources/static/product_image/image3.png")));

    ArrayList<ProductImage> productImages = new ArrayList<>();
    testImageList.forEach(
        image -> {
          FileUploadResult uploadResult =
              s3Service.uploadFile(image, "productImage/" + product.getSeller().getId());
          productImages.add(
              ProductImage.builder()
                  .imageUri(uploadResult.getFileServerUri())
                  .downLoadUrl(uploadResult.getDownLoadUrl())
                  .build());
        });

    product.updateProductImages(productImages);
  }

  public void saveProductContent(Product product) {
    ArrayList<MultipartFile> testImageList =
        new ArrayList<>(
            List.of(
                makeMultipartFile("src/test/resources/static/product_image/1.png"),
                makeMultipartFile("src/test/resources/static/product_image/2.png"),
                makeMultipartFile("src/test/resources/static/product_image/3.png")));

    ArrayList<ProductContent> productContents = new ArrayList<>();

    int ix = 0;
    for (ix = 0; ix < testImageList.size(); ix++) {
      FileUploadResult uploadResult =
          s3Service.uploadFile(testImageList.get(ix), "blockImage/" + product.getSeller().getId());
      productContents.add(makeProductImageBlock(uploadResult, ix));
    }
    for (int i = 0; i < 3; i++) {
      productContents.add(makeProductTextBlock("testContent" + ix, ++ix));
    }

    product.updateContents(productContents);
  }

  private ProductContent makeProductTextBlock(String content, long index) {
    TextBlock textBlock = new TextBlock(index, content);
    return ProductContent.builder()
        .type(BlockType.TEXT_TYPE)
        .content(JsonUtil.convertObjectToJson(textBlock))
        .build();
  }

  private ProductContent makeProductImageBlock(FileUploadResult uploadResult, long index) {
    ImageBlock imageBlock =
        new ImageBlock(index, uploadResult.getFileServerUri(), uploadResult.getDownLoadUrl());
    return ProductContent.builder()
        .type(BlockType.IMAGE_TYPE)
        .content(JsonUtil.convertObjectToJson(imageBlock))
        .build();
  }

  private MockMultipartFile makeMultipartFile(String filePath) {
    try {
      File file = new File(filePath);
      FileInputStream inputStream = new FileInputStream(file);
      return new MockMultipartFile(file.getName(), file.getName(), "image/jpeg", inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
