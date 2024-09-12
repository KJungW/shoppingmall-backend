package com.project.shoppingmall.service.refund;

import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.repository.RefundRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefundFindService {
  private final RefundRepository refundRepository;

  public Optional<Refund> findByIdWithPurchaseItemProduct(long refundId) {
    return refundRepository.findByIdWithPurchaseItemProduct(refundId);
  }

  public List<Refund> findAllProcessingStateRefundBySeller(long sellerID) {
    return refundRepository.findAllProcessingStateRefundBySeller(sellerID);
  }
}
