package com.project.shoppingmall.controller.chat.output;

import com.project.shoppingmall.dto.chat.ChatConnectRequestResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputRequestChatConnect {
  Long requesterId;
  Long chatRoomId;
  String chatConnectSecretNumber;

  public OutputRequestChatConnect(ChatConnectRequestResult requestResult) {
    this.requesterId = requestResult.getRequesterId();
    this.chatRoomId = requestResult.getChatRoomId();
    this.chatConnectSecretNumber = requestResult.getSecretNumber();
  }
}
