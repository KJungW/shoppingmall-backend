package com.project.shoppingmall.controller.refund;

import com.project.shoppingmall.controller.refund.input.InputAcceptRefund;
import com.project.shoppingmall.controller.refund.input.InputCompleteRefund;
import com.project.shoppingmall.controller.refund.input.InputRejectRefund;
import com.project.shoppingmall.controller.refund.input.InputRequestRefund;
import com.project.shoppingmall.controller.refund.output.OutputAcceptRefund;
import com.project.shoppingmall.controller.refund.output.OutputCompleteRefund;
import com.project.shoppingmall.controller.refund.output.OutputRejectRefund;
import com.project.shoppingmall.controller.refund.output.OutputRequestRefund;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.service.refund.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  @PutMapping("/refund/accept")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputAcceptRefund acceptRefund(@Valid @RequestBody InputAcceptRefund input) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Refund updatedRefund =
        refundService.acceptRefund(
            userDetail.getId(), input.getRefundId(), input.getResponseMessage());
    return new OutputAcceptRefund(updatedRefund.getId());
  }

  @PutMapping("/refund/complete")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputCompleteRefund completeRefund(@Valid @RequestBody InputCompleteRefund input) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Refund updatedRefund = refundService.completeRefund(userDetail.getId(), input.getRefundId());
    return new OutputCompleteRefund(updatedRefund.getId());
  }

  @PutMapping("/refund/reject")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRejectRefund rejectRefund(@Valid @RequestBody InputRejectRefund input) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Refund rejecteddRefund =
        refundService.rejectRefund(
            userDetail.getId(), input.getRefundId(), input.getResponseMessage());
    return new OutputRejectRefund(rejecteddRefund.getId());
  }
}
