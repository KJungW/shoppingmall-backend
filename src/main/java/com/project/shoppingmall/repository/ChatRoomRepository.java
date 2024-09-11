package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  @Query("select c from ChatRoom c " + "left join c.product p " + "where p.id = :productId")
  Optional<ChatRoom> findByProduct(@Param("productId") long productId);

  @Query(
      "select c from ChatRoom c "
          + "left join fetch c.buyer b "
          + "left join fetch c.seller s "
          + "where c.id = :chatId")
  Optional<ChatRoom> findByIdWithMember(@Param("chatId") long chatId);

  @Query("select c from ChatRoom c " + "left join c.buyer b " + "where b.id = :buyerId")
  List<ChatRoom> findAllByBuyer(@Param("buyerId") long buyerId);

  @Query("select c from ChatRoom c " + "left join c.seller s " + "where s.id = :sellerId")
  List<ChatRoom> findAllBySeller(@Param("sellerId") long sellerId);
}
