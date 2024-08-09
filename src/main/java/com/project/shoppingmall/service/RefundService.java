package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.NotRequestStateRefund;
import com.project.shoppingmall.exception.ProcessOrCompleteRefund;
import com.project.shoppingmall.repository.RefundRepository;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.type.RefundStateType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefundService {
  private final RefundRepository refundRepository;
  private final MemberService memberService;
  private final PurchaseItemService purchaseItemService;

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
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));

    if (!refund.getPurchaseItem().getProduct().getSeller().getId().equals(member.getId())) {
      throw new DataNotFound("다른 회원의 환불데이터 입니다.");
    }

    if (!refund.getState().equals(RefundStateType.REQUEST)) {
      throw new NotRequestStateRefund("Reqeuset상태의 환불이 아닙니다.");
    }

    refund.acceptRefund(responseContent);
    return refund;
  }

  public Optional<Refund> findByIdWithPurchaseItemProduct(long refundId) {
    return refundRepository.findByIdWithPurchaseItemProduct(refundId);
  }
}
