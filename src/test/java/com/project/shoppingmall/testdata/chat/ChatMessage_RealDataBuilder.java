package com.project.shoppingmall.testdata.chat;

import com.project.shoppingmall.entity.ChatMessage;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;

public class ChatMessage_RealDataBuilder {
  public static ChatMessage makeChatMessage(ChatRoom chatRoom, Member writer, String message) {
    return ChatMessage.builder()
        .chatRoomId(chatRoom.getId())
        .writerId(writer.getId())
        .message(message)
        .build();
  }
}
