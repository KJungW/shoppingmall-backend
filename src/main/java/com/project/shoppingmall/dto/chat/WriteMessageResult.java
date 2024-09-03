package com.project.shoppingmall.dto.chat;

import com.project.shoppingmall.entity.ChatMessage;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WriteMessageResult {
  private ChatMessage chatMessage;
  private ChatRoom chatRoom;
  private Member writer;
}
