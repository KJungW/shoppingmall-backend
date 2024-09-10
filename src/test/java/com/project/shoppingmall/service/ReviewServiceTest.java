package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.dto.review.ReviewMakeData;
import com.project.shoppingmall.dto.review.ReviewUpdateData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.AlreadyDeletedProduct;
import com.project.shoppingmall.exception.AlreadyExistReview;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ReviewBulkRepository;
import com.project.shoppingmall.repository.ReviewRepository;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.service.s3.S3Service;
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
  private ReviewFindService mockReviewFindService;
  private ReviewBulkRepository mockReviewBulkRepository;
  private PurchaseItemService mockPurchaseItemService;
  private ProductFindService mockProductFindService;
  private S3Service mockS3Service;

  @BeforeEach
  public void beforeEach() {
    mockReviewRepository = mock(ReviewRepository.class);
    mockReviewFindService = mock(ReviewFindService.class);
    mockReviewBulkRepository = mock(ReviewBulkRepository.class);
    mockPurchaseItemService = mock(PurchaseItemService.class);
    mockProductFindService = mock(ProductFindService.class);
    mockS3Service = mock(S3Service.class);
    target =
        new ReviewService(
            mockReviewRepository,
            mockReviewFindService,
            mockReviewBulkRepository,
            mockPurchaseItemService,
            mockProductFindService,
            mockS3Service);
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
    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenWriterId);

    Purchase givenPurchase = PurchaseBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchase, "buyer", givenBuyer);

    long givenProductId = 10L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    ReflectionTestUtils.setField(givenPurchaseItem, "productId", givenProductId);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", null);

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // - productService.findById() 세팅
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));

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
    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenWriterId);

    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchase, "buyer", givenBuyer);

    long givenProductId = 10L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    ReflectionTestUtils.setField(givenPurchaseItem, "productId", givenProductId);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", null);

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // - productService.findById() 세팅
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));

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
    long otherMemberId = 40L;
    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", otherMemberId);

    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchase, "buyer", givenBuyer);

    long givenProductId = 10L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    ReflectionTestUtils.setField(givenPurchaseItem, "productId", givenProductId);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", null);

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // - productService.findById() 세팅
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));

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
    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenWriterId);

    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchase, "buyer", givenBuyer);

    long givenProductId = 10L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    ReflectionTestUtils.setField(givenPurchaseItem, "productId", givenProductId);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", ReviewBuilder.fullData().build());

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // - productService.findById() 세팅
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));

    // when then
    assertThrows(AlreadyExistReview.class, () -> target.saveReview(givenReviewMakeData));
  }

  @Test
  @DisplayName("saveReview() : 이미 삭제된 상품에 대한 리뷰 작성")
  public void saveReview_alreadyDeletedProduct() throws IOException {
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
    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenWriterId);

    Purchase givenPurchase = PurchaseBuilder.fullData().buyer(givenBuyer).build();
    ReflectionTestUtils.setField(givenPurchase, "buyer", givenBuyer);

    long givenProductId = 20L;
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    ReflectionTestUtils.setField(givenPurchaseItem, "productId", givenProductId);
    ReflectionTestUtils.setField(givenPurchaseItem, "review", null);

    when(mockPurchaseItemService.findById(anyLong())).thenReturn(Optional.of(givenPurchaseItem));

    // - productService.findById() 세팅
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(AlreadyDeletedProduct.class, () -> target.saveReview(givenReviewMakeData));
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
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));

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
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));

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
}
