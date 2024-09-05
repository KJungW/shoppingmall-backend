package com.project.shoppingmall.service.chat_read_record;

import com.project.shoppingmall.entity.ChatReadRecord;
import com.project.shoppingmall.repository.ChatReadRecordRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatReadRecordFindService {
  private final ChatReadRecordRepository chatReadRecordRepository;

  public Optional<ChatReadRecord> findByChatRoomAndMember(long chatId, long memberId) {
    return chatReadRecordRepository.findByChatRoomAndMember(chatId, memberId);
  }

  public List<ChatReadRecord> findAllByChatRoomAndMember(List<Long> chatRoomIds, long memberId) {
    return chatReadRecordRepository.findAllByChatRoomAndMember(chatRoomIds, memberId);
  }
}
