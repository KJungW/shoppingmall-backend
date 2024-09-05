package com.project.shoppingmall.service.chat_room;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.chat.ChatRoomDto;
import com.project.shoppingmall.entity.ChatMessage;
import com.project.shoppingmall.entity.ChatReadRecord;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ChatRoomRetrieveRepository;
import com.project.shoppingmall.service.chat_read_record.ChatReadRecordFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomRetrieveService {
  private final ChatRoomRetrieveRepository chatRoomRetrieveRepository;
  private final ChatReadRecordFindService chatReadRecordFindService;
  private final MongoTemplate mongoTemplate;

  public SliceResult<ChatRoomDto> retrieveChatRoomBySeller(
      int sliceNumber, int sliceSize, long sellerId) {
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<ChatRoom> sliceResult =
        chatRoomRetrieveRepository.retrieveChatRoomBySeller(sellerId, pageRequest);

    List<Long> chatRoomIdList = sliceResult.getContent().stream().map(ChatRoom::getId).toList();
    List<ChatReadRecord> readRecordList =
        chatReadRecordFindService.findAllByChatRoomAndMember(chatRoomIdList, sellerId);

    List<ChatRoomDto> chatRoomDtoList =
        sliceResult.getContent().stream()
            .map(
                chatRoom -> {
                  ChatReadRecord chatReadRecord =
                      readRecordList.stream()
                          .filter(
                              readRecord ->
                                  readRecord.getChatRoom().getId().equals(chatRoom.getId()))
                          .findFirst()
                          .orElseThrow(
                              () -> new ServerLogicError("서로 매칭되지 않는 채팅방 목록과 채팅 읽기 기록이 존재합니다."));
                  Query query = new Query();
                  query.addCriteria(Criteria.where("chatRoomId").is(chatRoom.getId()));
                  query.addCriteria(
                      Criteria.where("createDate").gt(chatReadRecord.getLastModifiedDate()));
                  long unReadMessageCount = mongoTemplate.count(query, ChatMessage.class);
                  return new ChatRoomDto(chatRoom, unReadMessageCount);
                })
            .toList();

    return new SliceResult<>(sliceResult, chatRoomDtoList);
  }

  public SliceResult<ChatRoomDto> retrieveChatRoomByBuyer(
      int sliceNumber, int sliceSize, long buyerId) {
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<ChatRoom> sliceResult =
        chatRoomRetrieveRepository.retrieveChatRoomByBuyer(buyerId, pageRequest);

    List<Long> chatRoomIdList = sliceResult.getContent().stream().map(ChatRoom::getId).toList();
    List<ChatReadRecord> readRecordList =
        chatReadRecordFindService.findAllByChatRoomAndMember(chatRoomIdList, buyerId);

    List<ChatRoomDto> chatRoomDtoList =
        sliceResult.getContent().stream()
            .map(
                chatRoom -> {
                  ChatReadRecord chatReadRecord =
                      readRecordList.stream()
                          .filter(
                              readRecord ->
                                  readRecord.getChatRoom().getId().equals(chatRoom.getId()))
                          .findFirst()
                          .orElseThrow(
                              () -> new ServerLogicError("서로 매칭되지 않는 채팅방 목록과 채팅 읽기 기록이 존재합니다."));
                  Query query = new Query();
                  query.addCriteria(Criteria.where("chatRoomId").is(chatRoom.getId()));
                  query.addCriteria(
                      Criteria.where("createDate").gt(chatReadRecord.getLastModifiedDate()));
                  long unReadMessageCount = mongoTemplate.count(query, ChatMessage.class);
                  return new ChatRoomDto(chatRoom, unReadMessageCount);
                })
            .toList();

    return new SliceResult<>(sliceResult, chatRoomDtoList);
  }
}
