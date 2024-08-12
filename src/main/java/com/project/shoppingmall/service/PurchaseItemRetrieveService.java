package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseItemRetrieveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseItemRetrieveService {
  private final PurchaseItemRetrieveRepository purchaseItemRetrieveRepository;
  private final MemberService memberService;
  private final ProductService productService;

  public Slice<PurchaseItem> retrieveAllForSeller(
      long memberId, long productId, int sliceNumber, int sliceSize) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productService
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (!product.getSeller().getId().equals(member.getId())) {
      throw new DataNotFound("현재 회원은 해당 제품의 판매자가 아닙니다.");
    }

    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    return purchaseItemRetrieveRepository.findAllForSeller(product.getId(), pageRequest);
  }

  public Slice<PurchaseItem> retrieveRefundedAllForBuyer(
      long memberId, int sliceNumber, int sliceSize) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(
            sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));
    return purchaseItemRetrieveRepository.findRefundedAllForBuyer(member.getId(), pageRequest);
  }

  public Slice<PurchaseItem> retrieveRefundedAllForSeller(
      long sellerId, int sliceNumber, int sliceSize) {
    Member member =
        memberService
            .findById(sellerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(
            sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));
    return purchaseItemRetrieveRepository.findRefundedAllForSeller(member.getId(), pageRequest);
  }
}
