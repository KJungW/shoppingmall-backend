package com.project.shoppingmall.service.chat;

import com.project.shoppingmall.entity.ChatMessage;
import com.project.shoppingmall.entity.ChatReadRecord;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ChatReadRecordRepository;
import com.project.shoppingmall.service.member.MemberService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatReadRecordService {
  private final MemberService memberService;
  private final ChatReadRecordRepository chatReadRecordRepository;
  private final ChatRoomFindService chatRoomFindService;
  private final MongoTemplate mongoTemplate;

  @Transactional
  public ChatReadRecord make(long chatRoomId, long memberId) {
    ChatRoom chatRoom =
        chatRoomFindService
            .findByIdWithMember(chatRoomId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅방이 존재하지 않습니다."));
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    if (!chatRoom.checkMemberIsParticipant(member))
      throw new DataNotFound("입력된 회원은 현재 채팅방의 참가자가 아닙니다.");
    if (findByChatRoomAndMember(chatRoom.getId(), member.getId()).isPresent())
      throw new DataNotFound("이미 채팅 메세지 읽기 기록이 존재합니다.");
    ChatReadRecord readRecord = ChatReadRecord.builder().chatRoom(chatRoom).member(member).build();
    chatReadRecordRepository.save(readRecord);
    return readRecord;
  }

  @Transactional
  public void updateRecord(long chatRoomId, long memberId, String chatMessageId) {
    ChatReadRecord chatReadRecord =
        findByChatRoomAndMember(chatRoomId, memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅 읽기 기록이 존재하지 않습니다."));
    ChatMessage postChatMessage =
        Optional.ofNullable(mongoTemplate.findById(chatMessageId, ChatMessage.class))
            .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅 메세지가 존재하지 않습니다."));
    if (!postChatMessage.getChatRoomId().equals(chatRoomId))
      throw new DataNotFound("저장목표인 채팅 메세지는 현재 채팅방의 메세지가 아닙니다.");

    String latestReadMessageId = chatReadRecord.getLatestReadMessageId();
    if (latestReadMessageId != null && !latestReadMessageId.isBlank()) {
      ChatMessage preChatMessage =
          Optional.ofNullable(mongoTemplate.findById(latestReadMessageId, ChatMessage.class))
              .orElseThrow(() -> new DataNotFound("id에 해당하는 채팅 메세지가 존재하지 않습니다."));
      if (preChatMessage.getCreateDate().isAfter(postChatMessage.getCreateDate()))
        throw new DataNotFound("이미 읽은 채팅메세지는 읽기기록으로 남길 수 없습니다.");
    }
    ;

    chatReadRecord.updateLatestReadMessageId(postChatMessage.getId());
  }

  Optional<ChatReadRecord> findByChatRoomAndMember(long chatId, long memberId) {
    return chatReadRecordRepository.findByChatRoomAndMember(chatId, memberId);
  }
}
