package com.project.shoppingmall.controller.chat;

import com.project.shoppingmall.controller.chat.input.InputMakeChatRoom;
import com.project.shoppingmall.controller.chat.input.InputPublishMessage;
import com.project.shoppingmall.controller.chat.input.InputRequestChatConnect;
import com.project.shoppingmall.controller.chat.output.OutputMakeChatRoom;
import com.project.shoppingmall.controller.chat.output.OutputPublishMessage;
import com.project.shoppingmall.controller.chat.output.OutputRequestChatConnect;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.chat.ChatConnectRequestResult;
import com.project.shoppingmall.dto.chat.WriteMessageResult;
import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.entity.ChatRoom;
import com.project.shoppingmall.service.chat.ChatRoomService;
import com.project.shoppingmall.service.chat.ChatService;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
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
  private final JwtUtil jwtUtil;

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

  @MessageMapping("chat/message")
  public void publishMessage(
      @Valid @RequestBody InputPublishMessage input,
      @Header("Authorization") String authorizationHeader) {
    AccessTokenData accessTokenData = jwtUtil.decodeAccessToken(authorizationHeader.substring(7));
    WriteMessageResult result =
        chatService.writeMessage(
            input.getChatRoomId(), accessTokenData.getId(), input.getMessage());
    template.convertAndSend("/sub/chat/" + input.getChatRoomId(), new OutputPublishMessage(result));
  }
}
