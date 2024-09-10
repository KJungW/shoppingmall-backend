package com.project.shoppingmall.service.review;

import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.repository.ReviewRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewFindService {
  private final ReviewRepository reviewRepository;

  public Optional<Review> findById(long reviewId) {
    return reviewRepository.findById(reviewId);
  }

  public Optional<Review> findByIdWithWriter(long reviewId) {
    return reviewRepository.findByIdWithWriter(reviewId);
  }

  public List<Review> findByProduct(long productId) {
    return reviewRepository.findAllByProduct(productId);
  }
}
