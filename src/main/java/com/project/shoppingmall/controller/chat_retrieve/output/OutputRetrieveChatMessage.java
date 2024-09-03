package com.project.shoppingmall.controller.chat_retrieve.output;

import com.project.shoppingmall.dto.chat.ChatMessageDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputRetrieveChatMessage {
  List<ChatMessageDto> chatMessages;
}
