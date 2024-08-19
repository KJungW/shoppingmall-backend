package com.project.shoppingmall.service.refund;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.*;
import com.project.shoppingmall.repository.RefundRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.type.RefundStateType;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefundService {
  private final RefundRepository refundRepository;
  private final MemberService memberService;
  private final PurchaseItemService purchaseItemService;
  private final IamportClient iamportClient;

  @Value("${project_role.refund.create_possible_day}")
  private Integer refundSavePossibleDate;

  @Transactional
  public Refund saveRefund(
      Long memberId, long purchaseItemId, String requestTitle, String requestContent) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 회원이 존재하지 않습니다."));
    PurchaseItem purchaseItem =
        purchaseItemService
            .findByIdWithPurchaseAndRefund(purchaseItemId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 구매아이템이 존재하지 않습니다."));

    if (!purchaseItem.getPurchase().getBuyer().getId().equals(member.getId())) {
      throw new DataNotFound("현재 회원의 구매 아이템이 아닙니다.");
    }
    if (!purchaseItem.getPurchase().getState().equals(PurchaseStateType.COMPLETE)) {
      throw new DataNotFound("결제가 되지 않은 구매에 대해 환불은 불가능합니다.");
    }
    if (purchaseItem
        .getCreateDate()
        .isBefore(LocalDateTime.now().minusDays(refundSavePossibleDate))) {
      throw new PassedRefundRequest(refundSavePossibleDate + "일이 지난 구매에 대해서는 환불처리가 불가능합니다.");
    }
    if (!purchaseItemService.refundIsPossible(purchaseItem)) {
      throw new ProcessOrCompleteRefund("이미 진행중이거나 완료된 환불이 존재합니다.");
    }

    Refund newRefund =
        Refund.builder()
            .refundPrice(purchaseItem.getFinalPrice())
            .requestTitle(requestTitle)
            .requestContent(requestContent)
            .build();
    purchaseItem.addRefund(newRefund);

    refundRepository.save(newRefund);

    return newRefund;
  }

  @Transactional
  public Refund acceptRefund(long memberId, long refundId, String responseContent) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
    Refund refund =
        findByIdWithPurchaseItemProduct(refundId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 환불이 존재하지 않습니다."));

    if (!member.getId().equals(refund.getPurchaseItem().getSellerId())) {
      throw new DataNotFound("다른 회원의 판매상품에 대한 환불데이터 입니다.");
    }

    if (!refund.getState().equals(RefundStateType.REQUEST)) {
      throw new NotRequestStateRefund("Reqeuset상태의 환불이 아닙니다.");
    }

    refund.acceptRefund(responseContent);
    return refund;
  }

  @Transactional
  public Refund rejectRefund(Long memberId, long refundId, String responseContent) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
    Refund refund =
        findByIdWithPurchaseItemProduct(refundId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 환불이 존재하지 않습니다."));

    if (!member.getId().equals(refund.getPurchaseItem().getSellerId())) {
      throw new DataNotFound("다른 회원의 판매상품에 대한 환불데이터 입니다.");
    }
    if (!refund.getState().equals(RefundStateType.REQUEST)) {
      throw new NotRequestStateRefund("Reqeuset상태의 환불이 아닙니다.");
    }
    refund.rejectRefund(responseContent);
    return refund;
  }

  @Transactional
  public Refund completeRefund(long memberId, long refundId) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
    Refund refund =
        findByIdWithPurchaseItemProduct(refundId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 환불이 존재하지 않습니다."));

    if (!member.getId().equals(refund.getPurchaseItem().getSellerId())) {
      throw new DataNotFound("다른 회원의 판매상품에 대한 환불데이터 입니다.");
    }

    if (!refund.getState().equals(RefundStateType.ACCEPT)) {
      throw new NotAcceptStateRefund("Accepte 상태의 환불이 아닙니다.");
    }

    Purchase purchase = refund.getPurchaseItem().getPurchase();
    processRefund(purchase.getPaymentUid(), refund.getRefundPrice());
    refund.completeRefund();

    return refund;
  }

  public void processRefund(String paymentUid, int refundPrice) {
    IamportResponse<Payment> paymentIamportResponse =
        iamportClient.cancelPaymentByImpUid(
            new CancelData(paymentUid, true, new BigDecimal(refundPrice)));
    if (paymentIamportResponse.getResponse() == null) throw new FailRefundException("환불에 실패했습니다.");
  }

  public Optional<Refund> findByIdWithPurchaseItemProduct(long refundId) {
    return refundRepository.findByIdWithPurchaseItemProduct(refundId);
  }
}
