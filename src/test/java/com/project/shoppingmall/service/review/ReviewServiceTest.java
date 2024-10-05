package com.project.shoppingmall.service.review;

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
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.purchase.PurchaseBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import com.project.shoppingmall.test_entity.review.ReviewChecker;
import com.project.shoppingmall.test_entity.review.ReviewMakeDataBuilder;
import com.project.shoppingmall.test_entity.review.ReviewUpdateDataBuilder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

class ReviewServiceTest {
  private ReviewService target;
  private ReviewRepository mockReviewRepository;
  private ReviewFindService mockReviewFindService;
  private ReviewBulkRepository mockReviewBulkRepository;
  private PurchaseItemFindService mockPurchaseItemFindService;
  private ProductFindService mockProductFindService;
  private MemberFindService mockMemberFindService;
  private S3Service mockS3Service;

  @BeforeEach
  public void beforeEach() {
    mockReviewRepository = mock(ReviewRepository.class);
    mockReviewFindService = mock(ReviewFindService.class);
    mockReviewBulkRepository = mock(ReviewBulkRepository.class);
    mockPurchaseItemFindService = mock(PurchaseItemFindService.class);
    mockProductFindService = mock(ProductFindService.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockS3Service = mock(S3Service.class);
    target =
        new ReviewService(
            mockReviewRepository,
            mockReviewFindService,
            mockReviewBulkRepository,
            mockPurchaseItemFindService,
            mockProductFindService,
            mockMemberFindService,
            mockS3Service);
  }

  @Test
  @DisplayName("saveReview() : 정상흐름")
  public void saveReview_ok() {
    // given
    ReviewMakeData inputMakeData = ReviewMakeDataBuilder.make(30L, 40L);

    Member givenWriter = MemberBuilder.makeMember(inputMakeData.getWriterId());
    Product givenProduct = ProductBuilder.makeProduct(10L);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputMakeData.getPurchaseItemId(), givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(20L, givenWriter, List.of(givenPurchaseItem));
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(5L, 3.0);

    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenWriter));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when
    Review resultReview = target.saveReview(inputMakeData);

    // then
    check_s3Service_uploadFile(inputMakeData, givenProduct);
    checkRegisterReviewInPurchaseItem(resultReview, givenPurchaseItem);
    checkProductScoreAvgUpdate(givenScoreCalcResult, givenProduct);
    ReviewChecker.check(inputMakeData, givenProduct, givenUploadResult, resultReview);
  }

  @Test
  @DisplayName("saveReview() : 정상흐름 - 선택값 미입력")
  public void saveReview_ok_noTitleDescription() {
    // given
    ReviewMakeData inputMakeData = ReviewMakeDataBuilder.makeMinimum(30L, 40L);

    Member givenWriter = MemberBuilder.makeMember(inputMakeData.getWriterId());
    Product givenProduct = ProductBuilder.makeProduct(10L);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputMakeData.getPurchaseItemId(), givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(20L, givenWriter, List.of(givenPurchaseItem));
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(5L, 3.0);

    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenWriter));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when
    Review resultReview = target.saveReview(inputMakeData);

    // then
    check_s3Service_uploadFile_notRun();
    checkRegisterReviewInPurchaseItem(resultReview, givenPurchaseItem);
    checkProductScoreAvgUpdate(givenScoreCalcResult, givenProduct);
    ReviewChecker.checkReviewWithoutImage(inputMakeData, givenProduct, resultReview);
  }

  @Test
  @DisplayName("saveReview() : 다른 회원의 구매 아이템에 대한 리뷰 작성 시도")
  public void saveReview_otherMemberPurchaseItem() {
    // given
    ReviewMakeData inputMakeData = ReviewMakeDataBuilder.make(30L, 40L);

    Member givenOtherMember = MemberBuilder.makeMember(32532L);
    Product givenProduct = ProductBuilder.makeProduct(10L);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputMakeData.getPurchaseItemId(), givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(20L, givenOtherMember, List.of(givenPurchaseItem));
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(5L, 3.0);

    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenOtherMember));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when then
    assertThrows(DataNotFound.class, () -> target.saveReview(inputMakeData));
  }

  @Test
  @DisplayName("saveReview() : 리뷰 중복 작성")
  public void saveReview_duplicateReview() {
    // given
    ReviewMakeData inputMakeData = ReviewMakeDataBuilder.make(30L, 40L);

    Member givenWriter = MemberBuilder.makeMember(inputMakeData.getWriterId());
    Product givenProduct = ProductBuilder.makeProduct(10L);
    Review givenReview = ReviewBuilder.makeReview(40L, givenWriter, givenProduct);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(
            inputMakeData.getPurchaseItemId(), givenProduct, givenReview);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(20L, givenWriter, List.of(givenPurchaseItem));
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(5L, 3.0);

    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.of(givenProduct));
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenWriter));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when then
    assertThrows(AlreadyExistReview.class, () -> target.saveReview(inputMakeData));
  }

  @Test
  @DisplayName("saveReview() : 이미 삭제된 상품에 대한 리뷰 작성")
  public void saveReview_alreadyDeletedProduct() {
    // given
    ReviewMakeData inputMakeData = ReviewMakeDataBuilder.make(30L, 40L);

    Member givenWriter = MemberBuilder.makeMember(inputMakeData.getWriterId());
    Product givenProduct = ProductBuilder.makeProduct(10L);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputMakeData.getPurchaseItemId(), givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(20L, givenWriter, List.of(givenPurchaseItem));
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(5L, 3.0);

    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockProductFindService.findById(anyLong())).thenReturn(Optional.empty());
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenWriter));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when then
    assertThrows(AlreadyDeletedProduct.class, () -> target.saveReview(inputMakeData));
  }

  @Test
  @DisplayName("updateReview(): 정상흐름")
  public void updateReview_ok() {
    // given
    ReviewUpdateData inputUpdateData = ReviewUpdateDataBuilder.make(30L, 40L);

    Product givenProduct = ProductBuilder.makeProduct(40L);
    Member givenWriter = MemberBuilder.makeMember(inputUpdateData.getWriterId());
    Review givenReview =
        ReviewBuilder.makeReview(inputUpdateData.getReviewID(), givenWriter, givenProduct);
    String givenPreviousImageUri = givenReview.getReviewImageUri();
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(20L, 3.5d);

    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when
    Review updateResult = target.updateReview(inputUpdateData);

    // then
    check_s3Service_deleteFile(givenPreviousImageUri);
    check_s3Service_uploadFile(inputUpdateData, givenReview.getProduct());
    ReviewChecker.check(inputUpdateData, givenProduct, givenUploadResult, updateResult);
    checkProductScoreAvgUpdate(givenScoreCalcResult, givenReview.getProduct());
  }

  @Test
  @DisplayName("updateReview(): 업데이트 데이터에서 필수값이 아닌 필드에 빈값을 넣음")
  public void updateReview_inputNull() {
    // given
    ReviewUpdateData inputUpdateData = ReviewUpdateDataBuilder.makeMinimum(30L, 40L);

    Product givenProduct = ProductBuilder.makeProduct(40L);
    Member givenWriter = MemberBuilder.makeMember(inputUpdateData.getWriterId());
    Review givenReview =
        ReviewBuilder.makeReview(inputUpdateData.getReviewID(), givenWriter, givenProduct);
    String givenPreviousImageUri = givenReview.getReviewImageUri();
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(20L, 3.5d);

    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when
    Review updateResult = target.updateReview(inputUpdateData);

    // then
    check_s3Service_deleteFile(givenPreviousImageUri);
    check_s3Service_uploadFile_notRun();
    ReviewChecker.checkReviewWithoutImage(inputUpdateData, givenProduct, updateResult);
    checkProductScoreAvgUpdate(givenScoreCalcResult, givenReview.getProduct());
  }

  @Test
  @DisplayName("updateReview(): 다른 회원의 리뷰 수정시도")
  public void updateReview_otherMemberReview() {
    // given
    ReviewUpdateData inputUpdateData = ReviewUpdateDataBuilder.make(30L, 40L);

    Product givenProduct = ProductBuilder.makeProduct(40L);
    Member givenWriter = MemberBuilder.makeMember(inputUpdateData.getWriterId());
    Member otherMember = MemberBuilder.makeMember(3524L);
    Review givenReview =
        ReviewBuilder.makeReview(inputUpdateData.getReviewID(), otherMember, givenProduct);
    FileUploadResult givenUploadResult =
        new FileUploadResult("test image url", "test download url");
    ReviewScoresCalcResult givenScoreCalcResult = new ReviewScoresCalcResult(20L, 3.5d);

    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));
    when(mockS3Service.uploadFile(any(), anyString())).thenReturn(givenUploadResult);
    when(mockReviewRepository.calcReviewScoresInProduct(anyLong()))
        .thenReturn(givenScoreCalcResult);

    // when
    assertThrows(DataNotFound.class, () -> target.updateReview(inputUpdateData));
  }

  public void checkRegisterReviewInPurchaseItem(Review review, PurchaseItem target) {
    assertEquals(review.getId(), target.getReview().getId());
  }

  public void checkProductScoreAvgUpdate(
      ReviewScoresCalcResult givenScoreCalcResult, Product target) {
    assertEquals(givenScoreCalcResult.getScoreAverage(), target.getScoreAvg());
  }

  public void check_s3Service_deleteFile(String deletedImageUri) {
    ArgumentCaptor<String> imageUriCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).deleteFile(imageUriCaptor.capture());
    assertEquals(deletedImageUri, imageUriCaptor.getValue());
  }

  public void check_s3Service_uploadFile(ReviewMakeData makeData, Product product) {
    ArgumentCaptor<MultipartFile> multipartFileCaptor =
        ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> uploadUrlCaptor = ArgumentCaptor.forClass(String.class);

    verify(mockS3Service, times(1))
        .uploadFile(multipartFileCaptor.capture(), uploadUrlCaptor.capture());

    assertEquals(
        makeData.getReviewImage().getOriginalFilename(),
        multipartFileCaptor.getValue().getOriginalFilename());
    assertEquals("review/" + product.getId() + "/", uploadUrlCaptor.getValue());
  }

  public void check_s3Service_uploadFile(ReviewUpdateData updateData, Product product) {
    ArgumentCaptor<MultipartFile> multipartFileCaptor =
        ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> uploadUrlCaptor = ArgumentCaptor.forClass(String.class);

    verify(mockS3Service, times(1))
        .uploadFile(multipartFileCaptor.capture(), uploadUrlCaptor.capture());

    assertEquals(
        updateData.getReviewImage().getOriginalFilename(),
        multipartFileCaptor.getValue().getOriginalFilename());
    assertEquals("review/" + product.getId() + "/", uploadUrlCaptor.getValue());
  }

  public void check_s3Service_uploadFile_notRun() {
    verify(mockS3Service, times(0)).uploadFile(any(), any());
  }
}
