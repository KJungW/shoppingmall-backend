package com.project.shoppingmall.controller.chat;

import com.project.shoppingmall.controller.chat.input.InputMakeChatRoom;
import com.project.shoppingmall.controller.chat.input.InputRequestChatConnect;
import com.project.shoppingmall.controller.chat.output.OutputMakeChatRoom;
import com.project.shoppingmall.controller.chat.output.OutputRequestChatConnect;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.chat.ChatConnectRequestResult;
import com.project.shoppingmall.dto.chat.ChatMessage;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.service.ChatRoomService;
import com.project.shoppingmall.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {
  private final SimpMessageSendingOperations template;
  private final ChatService chatService;
  private final ChatRoomService chatRoomService;

  @PostMapping("/chat")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputMakeChatRoom makeChatRoom(@Valid @RequestBody InputMakeChatRoom input) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    ChatRoom chatRoom = chatRoomService.save(userDetail.getId(), input.getProductId());
    return new OutputMakeChatRoom(chatRoom.getId());
  }

  @PostMapping("/chat/connect/request")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRequestChatConnect requestChatConnect(
      @Valid @RequestBody InputRequestChatConnect input) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    ChatConnectRequestResult requestResult =
        chatService.requestChatConnect(userDetail.getId(), input.getChatRoomId());
    return new OutputRequestChatConnect(requestResult);
  }

  @MessageMapping("/message")
  public void publishMessage(@Valid @RequestBody ChatMessage chat) {
    System.out.println("chat.getChatRoomId() = " + chat.getChatRoomId());
    System.out.println("chat.getMessage() = " + chat.getMessage());
    template.convertAndSend("/sub/chat/" + chat.getChatRoomId(), chat);
  }
}
