package com.project.shoppingmall.service.purchase;

import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseRetrieveService {
  private final PurchaseRetrieveRepository purchaseRetrieveRepository;
  private final MemberFindService memberFindService;

  public Slice<Purchase> retrieveAllByMember(Long memberId, int sliceNumber, int sliceSize) {
    memberFindService
        .findById(memberId)
        .orElseThrow(() -> new DataNotFound("Id에 해당하는 회원이 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<Purchase> sliceResult = purchaseRetrieveRepository.findAllByBuyer(memberId, pageRequest);
    sliceResult.getContent().forEach(purchase -> purchase.getPurchaseItems().get(0));
    return sliceResult;
  }
}
