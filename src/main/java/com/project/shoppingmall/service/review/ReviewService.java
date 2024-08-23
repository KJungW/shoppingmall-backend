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
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.service.s3.S3Service;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final ReviewBulkRepository reviewBulkRepository;
  private final PurchaseItemService purchaseItemService;
  private final ProductService productService;
  private final S3Service s3Service;

  @Transactional
  public Review saveReview(ReviewMakeData makeData) {
    PurchaseItem purchaseItem =
        purchaseItemService
            .findById(makeData.getPurchaseItemId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 구매아이템이 존재하지 않습니다."));
    Product product =
        productService
            .findById(purchaseItem.getProductId())
            .orElseThrow(() -> new AlreadyDeletedProduct("이미 삭제된 상품입니다."));
    Member buyer = purchaseItem.getPurchase().getBuyer();

    if (!buyer.getId().equals(makeData.getWriterId())) {
      throw new DataNotFound("다른 회원의 구매아이템에 대해 리뷰작성을 시도하고 있습니다.");
    }
    if (!purchaseItem.writeReviewPossible()) {
      throw new AlreadyExistReview("이미 현재 구매 아이템에 대해 작성한 리뷰가 이미 존재합니다.");
    }

    FileUploadResult reviewImageUploadResult = new FileUploadResult("", "");
    if (makeData.getReviewImage() != null) {
      reviewImageUploadResult =
          s3Service.uploadFile(makeData.getReviewImage(), "review/" + product.getId() + "/");
    }
    ;

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

    ReviewScoresCalcResult scoreCalcResult = calcReviewScoresInProduct(product.getId());
    product.addScore(scoreCalcResult, makeData.getScore());

    return newReview;
  }

  @Transactional
  public Review updateReview(ReviewUpdateData updateData) {
    Review review =
        findById(updateData.getReviewID())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 Review 데이터가 존재하지 않습니다."));
    Product product = review.getProduct();

    if (!review.getWriter().getId().equals(updateData.getWriterId())) {
      throw new DataNotFound("다른 회원의 review를 수정하려고 시도하고 있습니다.");
    }

    if (!review.getReviewImageUri().isBlank()) {
      s3Service.deleteFile(review.getReviewImageUri());
    }

    FileUploadResult reviewImageUploadResult = new FileUploadResult("", "");
    if (updateData.getReviewImage() != null) {
      reviewImageUploadResult =
          s3Service.uploadFile(
              updateData.getReviewImage(), "review/" + review.getProduct().getId() + "/");
    }

    review.updateScore(updateData.getScore());
    review.updateTitle(updateData.getTitle());
    review.updateDescription(updateData.getDescription());
    review.updateReviewImage(
        reviewImageUploadResult.getFileServerUri(), reviewImageUploadResult.getDownLoadUrl());

    ReviewScoresCalcResult scoreCalcResult = calcReviewScoresInProduct(product.getId());
    product.refreshScore(scoreCalcResult);

    return review;
  }

  @Transactional
  public int banReviewsByWriterId(long writerId, boolean isBan) {
    return reviewBulkRepository.banReviewsByWriterId(writerId, isBan);
  }

  public Optional<Review> findById(long refundId) {
    return reviewRepository.findById(refundId);
  }

  public List<Review> findByProduct(long productId) {
    return reviewRepository.findAllByProduct(productId);
  }

  public ReviewScoresCalcResult calcReviewScoresInProduct(long productId) {
    return reviewRepository.calcReviewScoresInProduct(productId);
  }
}
