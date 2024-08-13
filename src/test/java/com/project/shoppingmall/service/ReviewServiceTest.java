package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.dto.review.ReviewMakeData;
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
    assertEquals(givenDownloadUrl, resultReview.getReviewImageUrl());
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
    assertEquals("", resultReview.getReviewImageUrl());
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
}
