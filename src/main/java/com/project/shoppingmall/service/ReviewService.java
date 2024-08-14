package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.dto.review.ReviewMakeData;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.AlreadyExistReview;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository reviewRepository;
  private final PurchaseItemService purchaseItemService;
  private final S3Service s3Service;

  @Transactional
  public Review saveReview(ReviewMakeData makeData) {
    PurchaseItem purchaseItem =
        purchaseItemService
            .findById(makeData.getPurchaseItemId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 구매아이템이 존재하지 않습니다."));
    Member buyer = purchaseItem.getPurchase().getBuyer();
    Product product = purchaseItem.getProduct();

    if (!buyer.getId().equals(makeData.getWriterId())) {
      throw new DataNotFound("다른 회원의 구매아이템에 대해 리뷰작성을 시도하고 있습니다.");
    }
    if (!purchaseItem.writeReviewPossible()) {
      throw new AlreadyExistReview("이미 현재 구매 아이템에 대해 작성한 리뷰가 이미 존재합니다.");
    }

    FileUploadResult reviewImageUploadResult = new FileUploadResult("", "");
    if (makeData.getReviewImage() != null) {
      reviewImageUploadResult =
          s3Service.uploadFile(
              makeData.getReviewImage(), "review/" + purchaseItem.getProduct().getId() + "/");
    }
    ;

    Review newReview =
        Review.builder()
            .writer(buyer)
            .product(purchaseItem.getProduct())
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

  public ReviewScoresCalcResult calcReviewScoresInProduct(long productId) {
    return reviewRepository.calcReviewScoresInProduct(productId);
  }

  @Transactional
  public void deleteReview(Long writerId, Long reviewId) {
    PurchaseItem purchaseItem =
        purchaseItemService
            .findByReviewId(reviewId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 리뷰가 존재하지 않습니다."));
    Review review = purchaseItem.getReview();
    Product product = review.getProduct();

    if (!review.getWriter().getId().equals(writerId)) {
      throw new DataNotFound("다른 회원의 리뷰입니다.");
    }

    purchaseItem.deleteReview();
    reviewRepository.delete(review);
    reviewRepository.flush();

    ReviewScoresCalcResult scoreCalcResult = calcReviewScoresInProduct(product.getId());
    product.refreshScore(scoreCalcResult);
  }
}
