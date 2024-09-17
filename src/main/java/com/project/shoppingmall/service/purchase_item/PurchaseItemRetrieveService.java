package com.project.shoppingmall.service.purchase_item;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.purchase.PurchaseItemDtoForSeller;
import com.project.shoppingmall.dto.refund.RefundPurchaseItemForSeller;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseItemRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
  private final MemberFindService memberFindService;
  private final ProductFindService productFindService;

  public SliceResult<PurchaseItemDtoForSeller> retrieveAllForSeller(
      long memberId, long productId, int sliceNumber, int sliceSize) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productFindService
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (!product.getSeller().getId().equals(member.getId())) {
      throw new DataNotFound("현재 회원은 해당 제품의 판매자가 아닙니다.");
    }

    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<PurchaseItem> queryResult =
        purchaseItemRetrieveRepository.findAllForSeller(product.getId(), pageRequest);
    List<PurchaseItemDtoForSeller> contents =
        queryResult.getContent().stream().map(PurchaseItemDtoForSeller::new).toList();
    return new SliceResult<PurchaseItemDtoForSeller>(queryResult, contents);
  }

  public SliceResult<PurchaseItemDtoForSeller> retrieveAllForSellerByDate(
      long memberId, int year, int month, int sliceNumber, int sliceSize) {
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
    LocalDateTime endDate = makeNextMonthLocalDateTime(year, month);
    Slice<PurchaseItem> queryResult =
        purchaseItemRetrieveRepository.findAllForSellerBetweenDate(
            memberId, startDate, endDate, pageRequest);
    List<PurchaseItemDtoForSeller> contents =
        queryResult.getContent().stream().map(PurchaseItemDtoForSeller::new).toList();
    return new SliceResult<PurchaseItemDtoForSeller>(queryResult, contents);
  }

  public Slice<PurchaseItem> retrieveRefundedAllForBuyer(
      long memberId, int sliceNumber, int sliceSize) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    PageRequest pageRequest =
        PageRequest.of(
            sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));
    return purchaseItemRetrieveRepository.findRefundedAllForBuyer(member.getId(), pageRequest);
  }

  public SliceResult<RefundPurchaseItemForSeller> retrieveRefundedAllForSeller(
      long sellerId, int sliceNumber, int sliceSize) {
    Member seller =
        memberFindService
            .findById(sellerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));

    PageRequest pageRequest =
        PageRequest.of(
            sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));
    Slice<PurchaseItem> purchaseItemSliceResult =
        purchaseItemRetrieveRepository.findRefundedAllForSeller(seller.getId(), pageRequest);

    List<PurchaseItem> purchaseItemList = purchaseItemSliceResult.getContent();
    List<Long> buyerIdList =
        purchaseItemList.stream()
            .map(purchaseItem -> purchaseItem.getPurchase().getBuyerId())
            .distinct()
            .toList();
    List<Member> buyerList = memberFindService.findAllByIds(buyerIdList);

    List<RefundPurchaseItemForSeller> dtoList =
        purchaseItemList.stream()
            .map(
                purchaseItem -> {
                  Optional<Member> targetBuyer =
                      buyerList.stream()
                          .filter(
                              buyer ->
                                  purchaseItem.getPurchase().getBuyerId().equals(buyer.getId()))
                          .findFirst();
                  return targetBuyer
                      .map(member -> new RefundPurchaseItemForSeller(purchaseItem, member))
                      .orElseGet(() -> new RefundPurchaseItemForSeller(purchaseItem));
                })
            .toList();

    return new SliceResult<RefundPurchaseItemForSeller>(purchaseItemSliceResult, dtoList);
  }

  private LocalDateTime makeNextMonthLocalDateTime(int year, int month) {
    if (month + 1 > 12) return LocalDateTime.of(year + 1, 1, 1, 0, 0);
    else return LocalDateTime.of(year, month + 1, 1, 0, 0);
  }
}
