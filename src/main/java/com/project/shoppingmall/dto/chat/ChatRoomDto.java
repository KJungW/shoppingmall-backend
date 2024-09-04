package com.project.shoppingmall.dto.chat;

import com.project.shoppingmall.dto.member.OtherMemberDto;
import com.project.shoppingmall.dto.product.ProductHeaderDto;
import com.project.shoppingmall.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomDto {
  private Long id;
  private OtherMemberDto buyer;
  private OtherMemberDto seller;
  private ProductHeaderDto product;
  private Long unreadMessageCount;

  public ChatRoomDto(ChatRoom chatRoom, long unreadMessageCount) {
    this.id = chatRoom.getId();
    this.buyer = new OtherMemberDto(chatRoom.getBuyer());
    this.seller = new OtherMemberDto(chatRoom.getSeller());
    this.product = new ProductHeaderDto(chatRoom.getProduct());
    this.unreadMessageCount = unreadMessageCount;
  }
}
