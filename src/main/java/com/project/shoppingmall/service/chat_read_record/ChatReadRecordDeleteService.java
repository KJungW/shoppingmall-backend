package com.project.shoppingmall.service.chat_read_record;

import com.project.shoppingmall.entity.ChatReadRecord;
import com.project.shoppingmall.repository.ChatReadRecordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatReadRecordDeleteService {
  private final ChatReadRecordRepository chatReadRecordRepository;

  public void deleteChatReadRecord(ChatReadRecord chatReadRecord) {
    chatReadRecordRepository.delete(chatReadRecord);
  }

  public void deleteChatReadRecordList(List<ChatReadRecord> chatReadRecordList) {
    chatReadRecordRepository.deleteAllInBatch(chatReadRecordList);
  }
}
