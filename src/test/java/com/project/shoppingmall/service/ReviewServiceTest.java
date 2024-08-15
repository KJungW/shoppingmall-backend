package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.dto.review.ReviewMakeData;
import com.project.shoppingmall.dto.review.ReviewUpdateData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.AlreadyExistReview;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ReviewRepository;
import com.project.shoppingmall.testdata.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

class ReviewServiceTest {
  private ReviewService target;
  private ReviewRepository mockReviewRepository;
  private PurchaseItemService mockPurchaseItemService;
  private S3Service mockS3Service;

  @BeforeEach
  public void beforeEach() {
    mockReviewRepository = mock(ReviewRepository.class);
    mockPurchaseItemService = mock(PurchaseItemService.class);
    mockS3Service = mock(S3Service.class);
    target = new ReviewService(mockReviewRepository, mockPurchaseItemService, mockS3Service);
  }

  @Test
  @DisplayName("saveReview() : 정상흐름")
  public void saveReview_ok() throws IOException {
    // given
    // - 인자 세팅
    long givenWriterId = 30L;
    long givenPurchaseItemId = 40L;
    int givenScore = 5;
    String givenTitle = "test review title";
    String givenDescription = "test review description";
    MultipartFile givenMockFile =
        new MockMultipartFile(
            "reviewSampleImage.png",
            new FileInputStream(new ClassPathResource("static/reviewSampleImage.png").getFile()));
    ReviewMakeData givenReviewMakeData =
        ReviewMakeData.builder()
            .writerId(givenWriterId)
            .purchaseItemId(givenPurchaseItemId)
            .score(givenScore)
            .title(givenTitle)
            .description(givenDescription)
            .reviewImage(givenMockFile)
            .build();

    // - purchaseItemService.findById() 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();

    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenWriterId);
    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 20L);
    ReflectionTestUtils.setField(givenPurchaseItem, "product", givenProduct);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", null);

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // - s3Service.uploadFile() 세팅
    String givenImageUrl = "test image url";
    String givenDownloadUrl = "test download url";
    when(mockS3Service.uploadFile(any(), anyString()))
        .thenReturn(new FileUploadResult(givenImageUrl, givenDownloadUrl));

    // - reviewRepository.calcReviewScoresInProduct() 세팅
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(5L, 3.0);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(new ReviewScoresCalcResult(5L, 3.0));

    // when
    Review resultReview = target.saveReview(givenReviewMakeData);

    // then
    // - s3Service.uploadFile() 인자 검증
    ArgumentCaptor<MultipartFile> multipartFileCaptor =
        ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> uploadUrlCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1))
        .uploadFile(multipartFileCaptor.capture(), uploadUrlCaptor.capture());

    assertEquals(
        givenReviewMakeData.getReviewImage().getOriginalFilename(),
        multipartFileCaptor.getValue().getOriginalFilename());
    assertEquals("review/" + givenProduct.getId() + "/", uploadUrlCaptor.getValue());

    // - givenPurchaseItem.review에 리뷰가 등록되었는지 검증
    assertSame(resultReview, givenPurchaseItem.getReview());

    // - givenProduct.scoreAvg 업데이트 검증
    double previousAvg = givenScoreCalcResult.getScoreAverage();
    long previousCnt = givenScoreCalcResult.getReviewCount();
    assertEquals(
        (previousAvg * previousCnt + givenScore) / (previousCnt + 1), givenProduct.getScoreAvg());

    // - DB에 저장된 Review 검증
    assertSame(givenBuyer, resultReview.getWriter());
    assertSame(givenProduct, resultReview.getProduct());
    assertEquals(givenScore, resultReview.getScore());
    assertEquals(givenTitle, resultReview.getTitle());
    assertFalse(resultReview.getIsBan());
    assertEquals(givenImageUrl, resultReview.getReviewImageUri());
    assertEquals(givenDownloadUrl, resultReview.getReviewImageDownloadUrl());
    assertEquals(givenDescription, resultReview.getDescription());
  }

  @Test
  @DisplayName("saveReview() : 정상흐름 - 선택값 미입력")
  public void saveReview_ok_noTitleDescription() throws IOException {
    // given
    // - 인자 세팅
    long givenWriterId = 30L;
    long givenPurchaseItemId = 40L;
    int givenScore = 5;
    String givenTitle = "test title";
    String givenDescription = null;
    MultipartFile givenMockFile = null;
    ReviewMakeData givenReviewMakeData =
        ReviewMakeData.builder()
            .writerId(givenWriterId)
            .purchaseItemId(givenPurchaseItemId)
            .score(givenScore)
            .title(givenTitle)
            .description(givenDescription)
            .reviewImage(givenMockFile)
            .build();

    // - purchaseItemService.findById() 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();

    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenWriterId);
    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 20L);
    ReflectionTestUtils.setField(givenPurchaseItem, "product", givenProduct);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", null);

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // - reviewRepository.calcReviewScoresInProduct() 세팅
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(5L, 3.0);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(new ReviewScoresCalcResult(5L, 3.0));

    // when
    Review resultReview = target.saveReview(givenReviewMakeData);

    // then
    // - givenPurchaseItem.review에 리뷰가 등록되었는지 검증
    assertSame(resultReview, givenPurchaseItem.getReview());

    // - givenProduct.scoreAvg 업데이트 검증
    double previousAvg = givenScoreCalcResult.getScoreAverage();
    long previousCnt = givenScoreCalcResult.getReviewCount();
    assertEquals(
        (previousAvg * previousCnt + givenScore) / (previousCnt + 1), givenProduct.getScoreAvg());

    // - DB에 저장된 Review 검증
    assertSame(givenBuyer, resultReview.getWriter());
    assertSame(givenProduct, resultReview.getProduct());
    assertEquals(givenScore, resultReview.getScore());
    assertEquals(givenTitle, resultReview.getTitle());
    assertFalse(resultReview.getIsBan());
    assertEquals("", resultReview.getReviewImageDownloadUrl());
    assertEquals("", resultReview.getDescription());
  }

  @Test
  @DisplayName("saveReview() : 다른 회원의 구매 아이템에 대한 리뷰 작성 시도")
  public void saveReview_otherMemberPurchaseItem() throws IOException {
    // given
    // - 인자 세팅
    long givenWriterId = 30L;
    long givenPurchaseItemId = 40L;
    int givenScore = 5;
    String givenTitle = "test title";
    String givenDescription = null;
    MultipartFile givenMockFile = null;
    ReviewMakeData givenReviewMakeData =
        ReviewMakeData.builder()
            .writerId(givenWriterId)
            .purchaseItemId(givenPurchaseItemId)
            .score(givenScore)
            .title(givenTitle)
            .description(givenDescription)
            .reviewImage(givenMockFile)
            .build();

    // - purchaseItemService.findById() 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();

    long givenBuyerId = 11L;
    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenBuyerId);
    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 20L);
    ReflectionTestUtils.setField(givenPurchaseItem, "product", givenProduct);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", null);

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // when then
    assertThrows(DataNotFound.class, () -> target.saveReview(givenReviewMakeData));
  }

  @Test
  @DisplayName("saveReview() : 리뷰 중복 작성")
  public void saveReview_duplicateReview() throws IOException {
    // given
    // - 인자 세팅
    long givenWriterId = 30L;
    long givenPurchaseItemId = 40L;
    int givenScore = 5;
    String givenTitle = "test title";
    String givenDescription = null;
    MultipartFile givenMockFile = null;
    ReviewMakeData givenReviewMakeData =
        ReviewMakeData.builder()
            .writerId(givenWriterId)
            .purchaseItemId(givenPurchaseItemId)
            .score(givenScore)
            .title(givenTitle)
            .description(givenDescription)
            .reviewImage(givenMockFile)
            .build();

    // - purchaseItemService.findById() 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();

    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenWriterId);
    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 20L);
    ReflectionTestUtils.setField(givenPurchaseItem, "product", givenProduct);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", ReviewBuilder.fullData().build());

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // when then
    assertThrows(AlreadyExistReview.class, () -> target.saveReview(givenReviewMakeData));
  }

  @Test
  @DisplayName("updateReview(): 정상흐름")
  public void updateReview_ok() throws IOException {
    // given
    long givenWriterId = 30L;
    long givenReviewId = 40L;
    int givenScore = 5;
    String givenTitle = "test review title";
    String givenDescription = "test review description";
    MultipartFile givenMockFile =
        new MockMultipartFile(
            "reviewSampleImage.png",
            new FileInputStream(new ClassPathResource("static/reviewSampleImage.png").getFile()));
    ReviewUpdateData givenUpdateData =
        ReviewUpdateData.builder()
            .writerId(givenWriterId)
            .reviewID(givenReviewId)
            .score(givenScore)
            .title(givenTitle)
            .description(givenDescription)
            .reviewImage(givenMockFile)
            .build();

    long givenProductId = 40L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    String givenImageUriBeforeUpdate = "testImageUriBeforeUpdate";
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    ReflectionTestUtils.setField(givenReview, "product", givenProduct);
    ReflectionTestUtils.setField(givenReview, "reviewImageUri", givenImageUriBeforeUpdate);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenWriterId);
    when(mockReviewRepository.findById(anyLong())).thenReturn(Optional.of(givenReview));

    String givenImageUrl = "test image url";
    String givenDownloadUrl = "test download url";
    when(mockS3Service.uploadFile(any(), anyString()))
        .thenReturn(new FileUploadResult(givenImageUrl, givenDownloadUrl));

    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(20L, 3.5d);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when
    Review updateResult = target.updateReview(givenUpdateData);

    // then
    ArgumentCaptor<String> imageUriCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).deleteFile(imageUriCaptor.capture());
    assertEquals(givenImageUriBeforeUpdate, imageUriCaptor.getValue());

    ArgumentCaptor<MultipartFile> imageCaptor = ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> imagePathCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).uploadFile(imageCaptor.capture(), imagePathCaptor.capture());
    assertSame(givenMockFile, imageCaptor.getValue());
    assertEquals("review/" + givenReview.getProduct().getId() + "/", imagePathCaptor.getValue());

    assertEquals(givenScore, updateResult.getScore());
    assertEquals(givenTitle, updateResult.getTitle());
    assertEquals(givenImageUrl, updateResult.getReviewImageUri());
    assertEquals(givenDownloadUrl, updateResult.getReviewImageDownloadUrl());
    assertEquals(givenDescription, updateResult.getDescription());
    assertEquals(givenScoreCalcResult.getScoreAverage(), givenReview.getProduct().getScoreAvg());
  }

  @Test
  @DisplayName("updateReview(): 업데이트 데이터에서 필수값이 아닌 필드에 빈값을 넣음")
  public void updateReview_inputNull() throws IOException {
    // given
    long givenWriterId = 30L;
    long givenReviewId = 40L;
    int givenScore = 5;
    String givenTitle = "test review title";
    String givenDescription = null;
    MultipartFile givenMockFile = null;
    ReviewUpdateData givenUpdateData =
        ReviewUpdateData.builder()
            .writerId(givenWriterId)
            .reviewID(givenReviewId)
            .score(givenScore)
            .title(givenTitle)
            .description(givenDescription)
            .reviewImage(givenMockFile)
            .build();

    long givenProductId = 40L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    String givenImageUriBeforeUpdate = "testImageUriBeforeUpdate";
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    ReflectionTestUtils.setField(givenReview, "product", givenProduct);
    ReflectionTestUtils.setField(givenReview, "reviewImageUri", givenImageUriBeforeUpdate);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenWriterId);
    when(mockReviewRepository.findById(anyLong())).thenReturn(Optional.of(givenReview));

    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(20L, 3.5d);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when
    Review updateResult = target.updateReview(givenUpdateData);

    // then
    ArgumentCaptor<String> imageUriCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).deleteFile(imageUriCaptor.capture());
    assertEquals(givenImageUriBeforeUpdate, imageUriCaptor.getValue());

    verify(mockS3Service, times(0)).uploadFile(any(), any());

    assertEquals(givenScore, updateResult.getScore());
    assertEquals(givenTitle, updateResult.getTitle());
    assertEquals("", updateResult.getReviewImageUri());
    assertEquals("", updateResult.getReviewImageDownloadUrl());
    assertEquals("", updateResult.getDescription());
    assertEquals(givenScoreCalcResult.getScoreAverage(), givenReview.getProduct().getScoreAvg());
  }

  @Test
  @DisplayName("updateReview(): 다른 회원의 리뷰 수정시도")
  public void updateReview_otherMemberReview() throws IOException {
    // given
    long givenWriterId = 30L;
    long givenReviewId = 40L;
    int givenScore = 5;
    String givenTitle = "test review title";
    String givenDescription = "test review description";
    MultipartFile givenMockFile =
        new MockMultipartFile(
            "reviewSampleImage.png",
            new FileInputStream(new ClassPathResource("static/reviewSampleImage.png").getFile()));
    ReviewUpdateData givenUpdateData =
        ReviewUpdateData.builder()
            .writerId(givenWriterId)
            .reviewID(givenReviewId)
            .score(givenScore)
            .title(givenTitle)
            .description(givenDescription)
            .reviewImage(givenMockFile)
            .build();

    long givenProductId = 40L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    long otherMemberId = 25L;
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    ReflectionTestUtils.setField(givenReview, "product", givenProduct);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", otherMemberId);
    when(mockReviewRepository.findById(anyLong())).thenReturn(Optional.of(givenReview));

    // when
    assertThrows(DataNotFound.class, () -> target.updateReview(givenUpdateData));
  }

  @Test
  @DisplayName("deleteReview() : 정상흐름")
  public void deleteReview_ok() throws IOException {
    // given
    long givenWriterId = 20L;
    long givenReviewId = 27L;

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 23L);

    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenWriterId);
    ReflectionTestUtils.setField(givenReview, "product", givenProduct);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "review", givenReview);
    when(mockPurchaseItemService.findByReviewId(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    ReviewScoresCalcResult givenReviewCalcResult = new ReviewScoresCalcResult(20L, 4.5);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenReviewCalcResult);

    // when
    target.deleteReview(givenWriterId, givenReviewId);

    // then
    assertNull(givenPurchaseItem.getReview());

    ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
    verify(mockReviewRepository, times(1)).delete(reviewCaptor.capture());
    assertSame(givenReview, reviewCaptor.getValue());

    assertEquals(givenReviewCalcResult.getScoreAverage(), givenProduct.getScoreAvg());
  }

  @Test
  @DisplayName("deleteReview() : 다른 회원의 리뷰를 제거하려고 시도")
  public void deleteReview_otherMemberReview() throws IOException {
    // given
    long givenWriterId = 20L;
    long givenReviewId = 27L;

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 23L);

    long givenOtherMemberId = 50L;
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenOtherMemberId);
    ReflectionTestUtils.setField(givenReview, "product", givenProduct);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "review", givenReview);
    when(mockPurchaseItemService.findByReviewId(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // when
    assertThrows(DataNotFound.class, () -> target.deleteReview(givenWriterId, givenReviewId));
  }
}
