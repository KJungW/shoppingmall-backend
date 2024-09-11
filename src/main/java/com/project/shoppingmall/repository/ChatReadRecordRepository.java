package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.ChatReadRecord;
import java.util.List;
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

  @Query(
      "select crr from ChatReadRecord crr "
          + "left join fetch crr.chatRoom c "
          + "left join fetch crr.member m "
          + "where c.id in :chatRoomIds "
          + "and m.id = :memberId ")
  List<ChatReadRecord> findAllByChatRoomAndMember(
      @Param("chatRoomIds") List<Long> chatRoomIds, @Param("memberId") long memberId);

  @Query(
      "select crr from ChatReadRecord crr "
          + "left join fetch crr.chatRoom c "
          + "where c.id = :chatRoomId ")
  List<ChatReadRecord> findAllByChatRoom(@Param("chatRoomId") long chatRoomId);
}
