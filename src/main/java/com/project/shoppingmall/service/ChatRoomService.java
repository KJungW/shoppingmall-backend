package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.AlreadyMakedChatRoom;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ChatRoomRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;
  private final MemberService memberService;
  private final ProductService productService;

  @Transactional
  public ChatRoom save(long memberId, long productId) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productService
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (findByProduct(product.getId()).isPresent())
      throw new AlreadyMakedChatRoom("해당 회원과 제품에 대해 이미 만들어진 채팅방이 존재합니다.");

    ChatRoom chatRoom = ChatRoom.builder().buyer(member).product(product).build();
    chatRoomRepository.save(chatRoom);

    return chatRoom;
  }

  public Optional<ChatRoom> findById(long chatId) {
    return chatRoomRepository.findById(chatId);
  }

  public Optional<ChatRoom> findByProduct(long productId) {
    return chatRoomRepository.findByProduct(productId);
  }

  public Optional<ChatRoom> findByIdWithMember(long chatId) {
    return chatRoomRepository.findByIdWithMember(chatId);
  }
}
