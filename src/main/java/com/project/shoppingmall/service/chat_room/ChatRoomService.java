package com.project.shoppingmall.service.chat_room;

import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.AlreadyMakedChatRoom;
import com.project.shoppingmall.exception.CannotCreateChatRoomAboutOwnProduct;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ChatRoomRepository;
import com.project.shoppingmall.service.chat_read_record.ChatReadRecordService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {
  private final ChatRoomRepository chatRoomRepository;
  private final MemberFindService memberFindService;
  private final ProductFindService productFindService;
  private final ChatReadRecordService chatReadRecordService;
  private final ChatRoomFindService chatRoomFindService;

  @Transactional
  public ChatRoom save(long memberId, long productId) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productFindService
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));

    if (product.getSeller().getId().equals(member.getId()))
      throw new CannotCreateChatRoomAboutOwnProduct("자신이 등록한 제품에 대해 자신이 채팅방을 생성할 수 없습니다.");

    if (chatRoomFindService.findByProduct(product.getId()).isPresent())
      throw new AlreadyMakedChatRoom("해당 회원과 제품에 대해 이미 만들어진 채팅방이 존재합니다.");

    ChatRoom chatRoom = ChatRoom.builder().buyer(member).product(product).build();
    chatRoomRepository.save(chatRoom);
    chatReadRecordService.make(chatRoom.getId(), member.getId());
    chatReadRecordService.make(chatRoom.getId(), product.getSeller().getId());
    return chatRoom;
  }
}
