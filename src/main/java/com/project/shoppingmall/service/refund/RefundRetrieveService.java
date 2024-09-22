package com.project.shoppingmall.service.refund;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.refund.RefundDto;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.RefundRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefundRetrieveService {
  private final RefundRetrieveRepository refundRetrieveRepository;
  private final MemberFindService memberFindService;
  private final PurchaseItemFindService purchaseItemFindService;

  public SliceResult<RefundDto> retrieveAllByPurchaseItem(
      long memberId, long purchaseItemId, int sliceNumber, int sliceSize) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    PurchaseItem purchaseItem =
        purchaseItemFindService
            .findById(purchaseItemId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 구매아이템이 존재하지 않습니다."));
    validateRetrieveRefundPermission(member, purchaseItem);
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<Refund> sliceResult =
        refundRetrieveRepository.findByPurchaseItem(purchaseItemId, pageRequest);
    List<RefundDto> contents = sliceResult.getContent().stream().map(RefundDto::new).toList();
    return new SliceResult<RefundDto>(sliceResult, contents);
  }

  private void validateRetrieveRefundPermission(Member member, PurchaseItem purchaseItem) {
    if (!member.getId().equals(purchaseItem.getPurchase().getBuyerId())
        && !member.getId().equals(purchaseItem.getSellerId())) {
      throw new DataNotFound("해당 환불데이터를 조회할 권한이 없는 회원입니다.");
    }
  }
}
