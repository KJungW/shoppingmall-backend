package com.project.shoppingmall.test_entity.chat;

import com.project.shoppingmall.entity.ChatReadRecord;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;

public class ChatReadRecord_RealDataBuilder {
  public static ChatReadRecord makeChatReadRecord(ChatRoom chatRoom, Member member) {
    return ChatReadRecord.builder().chatRoom(chatRoom).member(member).build();
  }
}
