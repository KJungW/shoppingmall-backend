package com.project.shoppingmall.service.chat;

import com.project.shoppingmall.dto.cache.ChatConnectCache;
import com.project.shoppingmall.dto.chat.ChatConnectRequestResult;
import com.project.shoppingmall.dto.chat.WriteMessageResult;
import com.project.shoppingmall.entity.ChatMessage;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.util.JsonUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {
  private final CacheRepository cacheRepository;
  private final ChatRoomService chatRoomService;
  private final MemberService memberService;
  private final MongoTemplate mongoTemplate;

  @Transactional
  public ChatConnectRequestResult requestChatConnect(long memberId, long chatroomId) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    ChatRoom chatRoom =
        chatRoomService
            .findById(chatroomId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅방이 존재하지 않습니다."));

    if (!chatRoom.checkMemberIsParticipant(member))
      throw new DataNotFound("현재 채팅방은 회원이 참여할 수 없는 채팅방입니다.");

    String chatConnectCacheKey = CacheTemplate.makeChatConnectCacheKey(member.getId());
    String secretNumber = UUID.randomUUID().toString();
    ChatConnectCache chatConnectCacheValue =
        new ChatConnectCache(member.getId(), chatRoom.getId(), secretNumber);
    cacheRepository.saveCache(
        chatConnectCacheKey, JsonUtil.convertObjectToJson(chatConnectCacheValue), 30L);

    return new ChatConnectRequestResult(member.getId(), chatRoom.getId(), secretNumber);
  }

  @Transactional
  public WriteMessageResult writeMessage(long chatRoomId, long writerId, String message) {
    ChatRoom chatRoom =
        chatRoomService
            .findByIdWithMember(chatRoomId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅방이 존재하지 않습니다."));
    Member writer =
        memberService
            .findById(writerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));

    if (!chatRoom.checkMemberIsParticipant(writer))
      throw new DataNotFound("현재 채팅방은 회원이 참여할 수 없는 채팅방입니다.");

    ChatMessage chatMessage =
        ChatMessage.builder()
            .chatRoomId(chatRoom.getId())
            .writerId(writer.getId())
            .message(message)
            .build();

    mongoTemplate.save(chatMessage);
    return new WriteMessageResult(chatRoom, writer, message);
  }
}
