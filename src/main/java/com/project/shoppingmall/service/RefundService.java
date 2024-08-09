package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ProcessOrCompleteRefund;
import com.project.shoppingmall.repository.RefundRepository;
import com.project.shoppingmall.type.PurchaseStateType;
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
}
