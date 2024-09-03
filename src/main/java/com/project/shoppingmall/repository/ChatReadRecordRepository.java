package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.ChatReadRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatReadRecordRepository extends JpaRepository<ChatReadRecord, Long> {

  @Query(
      "select crr from ChatReadRecord crr "
          + "left join fetch crr.chatRoom c "
          + "left join fetch crr.member m "
          + "where c.id = :chatRoomId "
          + "and m.id = :memberId")
  Optional<ChatReadRecord> findByChatRoomAndMember(
      @Param("chatRoomId") long chatRoomId, @Param("memberId") long memberId);
}
