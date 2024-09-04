package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRetrieveRepository extends JpaRepository<ChatRoom, Long> {
  @Query(
      "select cr from ChatRoom cr "
          + "left join fetch cr.buyer b "
          + "left join fetch cr.seller s "
          + "left join fetch cr.product p "
          + "left join fetch p.productType pt "
          + "where b.id = :buyerId")
  Slice<ChatRoom> retrieveChatRoomByBuyer(@Param("buyerId") long buyerId, Pageable pageable);

  @Query(
      "select cr from ChatRoom cr "
          + "left join fetch cr.buyer b "
          + "left join fetch cr.seller s "
          + "left join fetch cr.product p "
          + "left join fetch p.productType pt "
          + "where s.id = :sellerId")
  Slice<ChatRoom> retrieveChatRoomBySeller(@Param("sellerId") long sellerId, Pageable pageable);
}
