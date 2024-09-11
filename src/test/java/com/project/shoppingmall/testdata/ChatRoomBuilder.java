package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;

public class ChatRoomBuilder {
  public static ChatRoom makeChatRoom(Member buyer, Product product) {
    return ChatRoom.builder().buyer(buyer).product(product).build();
  }
}
