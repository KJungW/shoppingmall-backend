package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.ChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  @Query("select c from ChatRoom c " + "left join c.product p " + "where p.id = :productId")
  Optional<ChatRoom> findByProduct(@Param("productId") long productId);
}
