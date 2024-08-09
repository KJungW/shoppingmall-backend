package com.project.shoppingmall.controller.refund;

import com.project.shoppingmall.controller.refund.input.InputRequestRefund;
import com.project.shoppingmall.controller.refund.output.OutputRequestRefund;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundController {
  private final RefundService refundService;

  @PostMapping("/refund")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRequestRefund requestRefund(@Valid @RequestBody InputRequestRefund input) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Refund newRefund =
        refundService.saveRefund(
            userDetail.getId(),
            input.getPurchaseItemId(),
            input.getRequestTitle(),
            input.getRequestContent());
    return new OutputRequestRefund(newRefund.getId());
  }
}
