package com.project.shoppingmall.service.chat_room;

import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.repository.ChatRoomRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomFindService {
  private final ChatRoomRepository chatRoomRepository;

  public Optional<ChatRoom> findById(long chatId) {
    return chatRoomRepository.findById(chatId);
  }

  public Optional<ChatRoom> findByProduct(long productId) {
    return chatRoomRepository.findByProduct(productId);
  }

  public Optional<ChatRoom> findByIdWithMember(long chatId) {
    return chatRoomRepository.findByIdWithMember(chatId);
  }

  public List<ChatRoom> findAllByBuyer(long buyerId) {
    return chatRoomRepository.findAllByBuyer(buyerId);
  }

  public List<ChatRoom> findAllBySeller(long sellerId) {
    return chatRoomRepository.findAllBySeller(sellerId);
  }
}
