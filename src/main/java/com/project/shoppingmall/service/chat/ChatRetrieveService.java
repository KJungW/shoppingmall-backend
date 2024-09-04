package com.project.shoppingmall.service.chat;

import com.project.shoppingmall.dto.chat.ChatMessageDto;
import com.project.shoppingmall.entity.ChatMessage;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.member.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRetrieveService {
  private final ChatRoomFindService chatRoomFindService;
  private final ChatReadRecordService chatReadRecordService;
  private final MemberService memberService;
  private final MongoTemplate mongoTemplate;

  @Transactional
  public List<ChatMessageDto> retrieveInitChatMessages(
      int sliceSize, long chatRoomId, long listenerId) {
    ChatRoom chatRoom =
        chatRoomFindService
            .findByIdWithMember(chatRoomId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅방이 존재하지 않습니다."));
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    if (!chatRoom.checkMemberIsParticipant(listener)) throw new DataNotFound("회원이 참여중인 채팅방이 아닙니다.");

    Query query = new Query();
    query.addCriteria(Criteria.where("chatRoomId").is(chatRoom.getId()));
    query.with(Sort.by(Sort.Direction.DESC, "createDate"));
    query.limit(sliceSize);
    List<ChatMessage> chatMessages = mongoTemplate.find(query, ChatMessage.class);

    if (!chatMessages.isEmpty())
      chatReadRecordService.updateRecord(
          chatRoom.getId(), listener.getId(), chatMessages.get(0).getId());

    return chatMessages.stream().map(ChatMessageDto::new).toList();
  }

  public List<ChatMessageDto> retrieveChatMessages(
      int sliceSize, long chatRoomId, String startChatMessageId, long listenerId) {
    ChatRoom chatRoom =
        chatRoomFindService
            .findByIdWithMember(chatRoomId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅방이 존재하지 않습니다."));
    Member listener =
        memberService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    if (!chatRoom.checkMemberIsParticipant(listener)) throw new DataNotFound("회원이 참여중인 채팅방이 아닙니다.");

    Query startChatMessageQuery = new Query();
    startChatMessageQuery.addCriteria(Criteria.where("id").is(startChatMessageId));
    startChatMessageQuery.limit(1);
    List<ChatMessage> startChatQueryResult =
        mongoTemplate.find(startChatMessageQuery, ChatMessage.class);
    if (startChatQueryResult.isEmpty()) throw new DataNotFound("ID에 해당하는 채팅메세지가 존재하지 않습니다.");

    ChatMessage startChatMessage = startChatQueryResult.get(0);
    if (!startChatMessage.getChatRoomId().equals(chatRoom.getId()))
      throw new DataNotFound("조회된 채팅메세지는 다른 채팅방의 메세지 입니다.");

    Query query = new Query();
    query.addCriteria(Criteria.where("chatRoomId").is(chatRoom.getId()));
    query.addCriteria(Criteria.where("createDate").lt(startChatMessage.getCreateDate()));
    query.with(Sort.by(Sort.Direction.DESC, "createDate"));
    query.limit(sliceSize);
    List<ChatMessage> chatMessages = mongoTemplate.find(query, ChatMessage.class);
    return chatMessages.stream().map(ChatMessageDto::new).toList();
  }
}
