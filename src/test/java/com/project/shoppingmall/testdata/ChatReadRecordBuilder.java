package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.ChatReadRecord;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;

public class ChatReadRecordBuilder {
  public static ChatReadRecord makeChatReadRecord(ChatRoom chatRoom, Member member) {
    return ChatReadRecord.builder().chatRoom(chatRoom).member(member).build();
  }
}
