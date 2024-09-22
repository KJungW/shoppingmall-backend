package com.project.shoppingmall.testdata;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class IntegrationTestDataMaker {
  @Autowired private EntityManager em;
  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private S3Service s3Service;

  public ChatReadRecord saveChatReadRecord(ChatRoom chatRoom, Member member) {
    ChatReadRecord chatReadRecord = ChatReadRecordBuilder.makeChatReadRecord(chatRoom, member);
    em.persist(chatReadRecord);
    return chatReadRecord;
  }

  public ChatMessage saveChatMessage(ChatRoom chatRoom, Member writer, String message) {
    ChatMessage chatMessage = ChatMessageBuilder.makeChatMessage(chatRoom, writer, message);
    mongoTemplate.insert(chatMessage);
    return chatMessage;
  }

  public ChatRoom saveChatRoom(Member buyer, Product product) {
    ChatRoom chatRoom = ChatRoomBuilder.makeChatRoom(buyer, product);
    em.persist(chatRoom);
    return chatRoom;
  }

  public ReviewReport saveReviewReport(Member reporter, Review review) {
    ReviewReport reviewReport = ReviewReportBuilder.makeProcessedReviewReport(reporter, review);
    em.persist(reviewReport);
    return reviewReport;
  }

  public ProductReport saveProductReport(Member reporter, Product product) {
    ProductReport productReport =
        ProductReportBuilder.makeNoProcessedProductReport(reporter, product);
    em.persist(productReport);
    return productReport;
  }

  public Alarm saveAlarm(Member listener) throws IOException {
    Alarm alarm = AlarmBuilder.makeMemberBanAlarm(listener);
    em.persist(alarm);
    return alarm;
  }

  public BasketItem saveBasketItem(Member member, Product product) {
    BasketItem basketItem = BasketItemBuilder.makeBasketItem(member, product);
    em.persist(basketItem);
    return basketItem;
  }

  public Review saveReview(Member reviewer, Product product, PurchaseItem purchaseItem) {
    Review review = ReviewBuilder.makeReview(reviewer, product);
    purchaseItem.registerReview(review);
    em.persist(review);
    return review;
  }

  public PurchaseItem savePurchaseItem(Product product, Member buyer) {
    PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(product);
    Purchase purchase =
        PurchaseBuilder.makePurchase(
            buyer, new ArrayList<>(List.of(purchaseItem)), PurchaseStateType.COMPLETE);
    em.persist(purchase);
    return purchaseItem;
  }

  public Product saveProduct(Member seller, ProductType type) {
    Product product = ProductBuilder.makeNoBannedProduct(seller, type);
    em.persist(product);
    return product;
  }

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

  public void saveMemberProfileImage(Member member) throws IOException {
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
