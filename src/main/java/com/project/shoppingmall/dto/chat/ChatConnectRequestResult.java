package com.project.shoppingmall.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatConnectRequestResult {
  Long requesterId;
  Long chatRoomId;
  String secretNumber;
}
