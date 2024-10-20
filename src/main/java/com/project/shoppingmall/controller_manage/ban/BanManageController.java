package com.project.shoppingmall.controller_manage.ban;

import com.project.shoppingmall.controller_manage.ban.output.OutputBanMember;
import com.project.shoppingmall.controller_manage.ban.output.OutputBanProduct;
import com.project.shoppingmall.controller_manage.ban.output.OutputBanReview;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.service_manage.ban.BanManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BanManageController {
  private final BanManageService banManageService;

  @PostMapping("/member/ban")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputBanMember banMember(
      @RequestParam("memberId") Long memberId, @RequestParam("isBan") Boolean isBan) {
    Member member = banManageService.banMember(memberId, isBan);
    return new OutputBanMember(member.getId());
  }

  @PostMapping("/product/ban")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputBanProduct banProduct(
      @RequestParam("productId") Long productId, @RequestParam("isBan") Boolean isBan) {
    Product product = banManageService.banProduct(productId, isBan);
    return new OutputBanProduct(product.getId());
  }

  @PostMapping("/review/ban")
  @PreAuthorize("hasAnyRole('ROLE_ROOT_MANAGER', 'ROLE_COMMON_MANAGER')")
  public OutputBanReview banReview(
      @RequestParam("reviewId") Long reviewId, @RequestParam("isBan") Boolean isBan) {
    Review review = banManageService.banReview(reviewId, isBan);
    return new OutputBanReview(review.getId());
  }
}
