package com.project.shoppingmall.service.chat_room;

import com.project.shoppingmall.entity.ChatMessage;
import com.project.shoppingmall.entity.ChatReadRecord;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.repository.ChatRoomRepository;
import com.project.shoppingmall.service.chat_read_record.ChatReadRecordDeleteService;
import com.project.shoppingmall.service.chat_read_record.ChatReadRecordFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomDeleteService {
  private final ChatRoomRepository chatRoomRepository;
  private final ChatReadRecordFindService chatReadRecordFindService;
  private final ChatReadRecordDeleteService chatReadRecordDeleteService;
  private final MongoTemplate mongoTemplate;

  public void deleteChatRoom(ChatRoom chatRoom) {
    // 채팅 읽기 기록 제거
    List<ChatReadRecord> chatReadRecords =
        chatReadRecordFindService.findAllByChatRoom(chatRoom.getId());
    chatReadRecordDeleteService.deleteChatReadRecordList(chatReadRecords);

    // 채팅 메세지 삭제
    Query query = new Query();
    query.addCriteria(Criteria.where("chatRoomId").is(chatRoom.getId()));
    mongoTemplate.remove(query, ChatMessage.class);

    // 채팅방 삭제
    chatRoomRepository.delete(chatRoom);
  }

  public void deleteChatRoomList(List<ChatRoom> chatRoomList) {
    chatRoomList.forEach(this::deleteChatRoom);
  }
}
