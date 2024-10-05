package com.project.shoppingmall.test_entity.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.review.ReviewMakeData;
import com.project.shoppingmall.dto.review.ReviewUpdateData;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;

public class ReviewChecker {
  public static void check(
      ReviewMakeData makeData, Product product, FileUploadResult uploadResult, Review target) {
    assertEquals(makeData.getWriterId(), target.getWriter().getId());
    assertEquals(product.getId(), target.getProduct().getId());
    assertEquals(makeData.getScore(), target.getScore());
    assertEquals(makeData.getTitle(), target.getTitle());
    assertFalse(target.getIsBan());
    assertEquals(uploadResult.getFileServerUri(), target.getReviewImageUri());
    assertEquals(uploadResult.getDownLoadUrl(), target.getReviewImageDownloadUrl());
    assertEquals(makeData.getDescription(), target.getDescription());
  }

  public static void check(
      ReviewUpdateData updateData, Product product, FileUploadResult uploadResult, Review target) {
    assertEquals(updateData.getReviewID(), target.getId());
    assertEquals(updateData.getWriterId(), target.getWriter().getId());
    assertEquals(product.getId(), target.getProduct().getId());
    assertEquals(updateData.getScore(), target.getScore());
    assertEquals(updateData.getTitle(), target.getTitle());
    assertFalse(target.getIsBan());
    assertEquals(uploadResult.getFileServerUri(), target.getReviewImageUri());
    assertEquals(uploadResult.getDownLoadUrl(), target.getReviewImageDownloadUrl());
    assertEquals(updateData.getDescription(), target.getDescription());
  }

  public static void checkReviewWithoutImage(
      ReviewMakeData makeData, Product product, Review target) {
    assertEquals(makeData.getWriterId(), target.getWriter().getId());
    assertEquals(product.getId(), target.getProduct().getId());
    assertEquals(makeData.getScore(), target.getScore());
    assertEquals(makeData.getTitle(), target.getTitle());
    assertFalse(target.getIsBan());
    assertEquals("", target.getReviewImageUri());
    assertEquals("", target.getReviewImageDownloadUrl());
    assertEquals("", target.getDescription());
  }

  public static void checkReviewWithoutImage(
      ReviewUpdateData updateData, Product product, Review target) {
    assertEquals(updateData.getReviewID(), target.getId());
    assertEquals(updateData.getWriterId(), target.getWriter().getId());
    assertEquals(product.getId(), target.getProduct().getId());
    assertEquals(updateData.getScore(), target.getScore());
    assertEquals(updateData.getTitle(), target.getTitle());
    assertFalse(target.getIsBan());
    assertEquals("", target.getReviewImageUri());
    assertEquals("", target.getReviewImageDownloadUrl());
    assertEquals("", target.getDescription());
  }
}
