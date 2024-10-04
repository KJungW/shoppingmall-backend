package com.project.shoppingmall.service.review;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final ReviewFindService reviewFindService;
  private final ReviewBulkRepository reviewBulkRepository;
  private final PurchaseItemFindService purchaseItemFindService;
  private final ProductFindService productFindService;
  private final MemberFindService memberFindService;
  private final S3Service s3Service;

  @Transactional
  public Review saveReview(ReviewMakeData makeData) {
    PurchaseItem purchaseItem = loadPurchaseItem(makeData.getPurchaseItemId());
    Member buyer = loadPurchaseItemBuyer(purchaseItem);
    Product product = loadPurchaseItemProduct(purchaseItem);

    checkMemberIsPurchaseItemBuyer(makeData.getWriterId(), buyer);
    checkDuplicateReview(purchaseItem);

    FileUploadResult reviewImageUploadResult =
        uploadReviewImage(makeData.getReviewImage(), "review/" + product.getId() + "/");

    Review newReview =
        Review.builder()
            .writer(buyer)
            .product(product)
            .score(makeData.getScore())
            .title(makeData.getTitle())
            .reviewImageUri(reviewImageUploadResult.getFileServerUri())
            .reviewImageDownloadUrl(reviewImageUploadResult.getDownLoadUrl())
            .description(makeData.getDescription())
            .build();
    purchaseItem.registerReview(newReview);
    reviewRepository.save(newReview);
    reviewRepository.flush();

    updateProductScore(product);
    return newReview;
  }

  @Transactional
  public Review updateReview(ReviewUpdateData updateData) {
    Review review = loadReview(updateData.getReviewID());

    checkMemberIsReviewWriter(updateData.getWriterId(), review);

    deletePreviousReviewImage(review);
    FileUploadResult reviewImageUploadResult =
        uploadReviewImage(
            updateData.getReviewImage(), "review/" + review.getProduct().getId() + "/");

    review.updateScore(updateData.getScore());
    review.updateTitle(updateData.getTitle());
    review.updateDescription(updateData.getDescription());
    review.updateReviewImage(
        reviewImageUploadResult.getFileServerUri(), reviewImageUploadResult.getDownLoadUrl());
    reviewRepository.flush();

    updateProductScore(review.getProduct());
    return review;
  }

  @Transactional
  public int banReviewsByWriterId(long writerId, boolean isBan) {
    return reviewBulkRepository.banReviewsByWriterId(writerId, isBan);
  }

  public void updateProductScore(Product product) {
    ReviewScoresCalcResult scoreCalcResult =
        reviewRepository.calcReviewScoresInProduct(product.getId());
    product.refreshScore(scoreCalcResult);
  }

  private PurchaseItem loadPurchaseItem(long purchaseItemId) {
    return purchaseItemFindService
        .findById(purchaseItemId)
        .orElseThrow(() -> new DataNotFound("Id에 해당하는 구매아이템이 존재하지 않습니다."));
  }

  private Member loadPurchaseItemBuyer(PurchaseItem purchaseItem) {
    return memberFindService
        .findById(purchaseItem.getPurchase().getBuyerId())
        .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
  }

  private Product loadPurchaseItemProduct(PurchaseItem purchaseItem) {
    return productFindService
        .findById(purchaseItem.getProductId())
        .orElseThrow(() -> new AlreadyDeletedProduct("이미 삭제된 상품입니다."));
  }

  private Review loadReview(long reviewId) {
    return reviewFindService
        .findById(reviewId)
        .orElseThrow(() -> new DataNotFound("Id에 해당하는 Review 데이터가 존재하지 않습니다."));
  }

  private void checkMemberIsPurchaseItemBuyer(long memberId, Member purchaseItemBuyer) {
    if (!purchaseItemBuyer.getId().equals(memberId)) {
      throw new DataNotFound("현재 회원은 구매아이템의 구매자가 아닙니다.");
    }
  }

  private void checkDuplicateReview(PurchaseItem purchaseItem) {
    if (!purchaseItem.writeReviewPossible()) {
      throw new AlreadyExistReview("이미 현재 구매 아이템에 대해 작성한 리뷰가 이미 존재합니다.");
    }
  }

  private void checkMemberIsReviewWriter(long memberId, Review review) {
    if (!review.getWriter().getId().equals(memberId)) {
      throw new DataNotFound("다른 회원의 review를 수정하려고 시도하고 있습니다.");
    }
  }

  private FileUploadResult uploadReviewImage(MultipartFile reviewImage, String uri) {
    FileUploadResult reviewImageUploadResult = new FileUploadResult("", "");
    if (reviewImage != null) reviewImageUploadResult = s3Service.uploadFile(reviewImage, uri);
    return reviewImageUploadResult;
  }

  private void deletePreviousReviewImage(Review review) {
    if (!review.getReviewImageUri().isBlank()) {
      s3Service.deleteFile(review.getReviewImageUri());
    }
  }
}
