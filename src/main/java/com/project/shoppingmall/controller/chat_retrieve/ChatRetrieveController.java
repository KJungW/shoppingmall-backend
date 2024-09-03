package com.project.shoppingmall.controller.chat_retrieve;

import com.project.shoppingmall.controller.chat_retrieve.output.OutputRetrieveChatMessage;
import com.project.shoppingmall.controller.chat_retrieve.output.OutputRetrieveInitChatMessage;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.chat.ChatMessageDto;
import com.project.shoppingmall.service.chat.ChatRetrieveService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatRetrieveController {
  private final ChatRetrieveService chatRetrieveService;

  @GetMapping("/chat/messages/latest")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrieveInitChatMessage retrieveInitChatMessages(
      @RequestParam("sliceSize") Integer sliceSize, @RequestParam("chatRoomId") Long chatRoomId) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    List<ChatMessageDto> chatMessages =
        chatRetrieveService.retrieveInitChatMessages(sliceSize, chatRoomId, userDetail.getId());
    return new OutputRetrieveInitChatMessage(chatMessages);
  }

  @GetMapping("/chat/messages")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrieveChatMessage retrieveChatMessages(
      @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("chatRoomId") Integer chatRoomId,
      @RequestParam("startChatMessageId") String startChatMessageId) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    List<ChatMessageDto> chatMessages =
        chatRetrieveService.retrieveChatMessages(
            sliceSize, chatRoomId, startChatMessageId, userDetail.getId());
    return new OutputRetrieveChatMessage(chatMessages);
  }
}
