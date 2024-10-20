package com.project.shoppingmall.controller.chat.output;

import com.project.shoppingmall.dto.chat.WriteMessageResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputPublishMessage {
  private String chatMessageId;
  private Long chatRoomId;
  private Long writerId;
  private String writerName;
  private String message;

  public OutputPublishMessage(WriteMessageResult result) {
    this.chatMessageId = result.getChatMessage().getId();
    this.chatRoomId = result.getChatRoom().getId();
    this.writerId = result.getWriter().getId();
    this.writerName = result.getWriter().getNickName();
    this.message = result.getChatMessage().getMessage();
  }
}
