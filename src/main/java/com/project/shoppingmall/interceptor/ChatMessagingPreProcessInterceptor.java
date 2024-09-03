package com.project.shoppingmall.interceptor;

import com.project.shoppingmall.dto.cache.ChatConnectCache;
import com.project.shoppingmall.dto.exception.ErrorResult;
import com.project.shoppingmall.dto.token.AccessTokenData;
import com.project.shoppingmall.exception.IncorrectChatDataInput;
import com.project.shoppingmall.exception.IncorrectChatUrl;
import com.project.shoppingmall.exception.JwtTokenException;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.type.ErrorCode;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessagingPreProcessInterceptor implements ChannelInterceptor {
  private final JwtUtil jwtUtil;
  private final CacheRepository cacheRepository;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    try {
      switch (accessor.getMessageType()) {
        case SUBSCRIBE -> {
          String destination = accessor.getDestination();
          if (destination == null || !destination.startsWith("/sub/chat/"))
            throw new IncorrectChatUrl("잘못된 경로입니다. 다시 한번 확인해주세요!");
          AccessTokenData accessTokenData = checkJwtAccessToken(accessor);
          ChatConnectCache chatConnectCache = getChatConnectCache(accessTokenData);
          checkRequesterInChatConnectCache(chatConnectCache, accessTokenData);
          checkChatRoomInChatConnectCache(chatConnectCache, accessor);
          checkSecretNumberInChatConnectCache(chatConnectCache, accessor);
          deleteChatConnectCache(accessTokenData);
        }
        case MESSAGE -> {
          String destination = accessor.getDestination();
          if (destination == null || !destination.startsWith("/pub/chat/message"))
            throw new IncorrectChatUrl("잘못된 경로입니다. 다시 한번 확인해주세요!");
          checkJwtAccessToken(accessor);
        }
      }
    } catch (IncorrectChatUrl ex) {
      ErrorResult errorResult = new ErrorResult(ErrorCode.INCORRECT_CHAT_URL, "잘못된 URL 경로입니다.");
      String errorResultJson = JsonUtil.convertObjectToJson(errorResult);
      throw new IncorrectChatDataInput(errorResultJson);
    } catch (JwtTokenException ex) {
      ErrorResult errorResult = new ErrorResult(ErrorCode.UNAUTHORIZED, "토큰이 존재하지 않거나 만료되었습니다.");
      String errorResultJson = JsonUtil.convertObjectToJson(errorResult);
      throw new JwtTokenException(errorResultJson);
    } catch (IncorrectChatDataInput ex) {
      ErrorResult errorResult = new ErrorResult(ErrorCode.BAD_INPUT, "잘못된 데이터가 입력되엇습니다.");
      String errorResultJson = JsonUtil.convertObjectToJson(errorResult);
      throw new IncorrectChatDataInput(errorResultJson);
    } catch (Exception ex) {
      ErrorResult errorResult = new ErrorResult(ErrorCode.SERVER_ERROR, "예상치 못한 에러발생");
      String errorResultJson = JsonUtil.convertObjectToJson(errorResult);
      throw new IncorrectChatDataInput(errorResultJson);
    }
    return message;
  }

  private AccessTokenData checkJwtAccessToken(StompHeaderAccessor accessor) {
    String authorizationHeader = String.valueOf(accessor.getNativeHeader("Authorization"));
    if (authorizationHeader == null) throw new JwtTokenException("Jwt Access 토큰이 존재하지 않습니다.");
    try {
      String accessToken = authorizationHeader.substring(8, authorizationHeader.length() - 1);
      return jwtUtil.decodeAccessToken(accessToken);
    } catch (Exception ex) {
      throw new JwtTokenException("Jwt Access 토큰이 유효하지 않습니다.");
    }
  }

  private ChatConnectCache getChatConnectCache(AccessTokenData accessTokenData) {
    String chatConnectKey = CacheTemplate.makeChatConnectCacheKey(accessTokenData.getId());
    String chatConnectCacheJson =
        cacheRepository
            .getCache(chatConnectKey)
            .orElseThrow(
                () -> new IncorrectChatDataInput("채팅 참여에 대한 캐시가 존재하지 않습니다. 참여요청 API를 먼저 실행해주세요."));
    return JsonUtil.convertJsonToObject(chatConnectCacheJson, ChatConnectCache.class);
  }

  private void deleteChatConnectCache(AccessTokenData accessTokenData) {
    String chatConnectKey = CacheTemplate.makeChatConnectCacheKey(accessTokenData.getId());
    cacheRepository.removeCache(chatConnectKey);
  }

  private void checkRequesterInChatConnectCache(ChatConnectCache cache, AccessTokenData tokenData) {
    if (!cache.getRequesterId().equals(tokenData.getId()))
      throw new IncorrectChatDataInput("현재 회원ID와 조회된 채팅참여 캐시 데이터가 맞지 않습니다.");
  }

  private void checkChatRoomInChatConnectCache(
      ChatConnectCache cache, StompHeaderAccessor accessor) {
    String[] destinationPart = accessor.getDestination().split("/");
    String chatRoomId = destinationPart[destinationPart.length - 1];
    if (!cache.getChatRoomId().toString().equals(chatRoomId))
      throw new IncorrectChatDataInput("입력된 채팅방ID와 조회된 채팅참여 캐시 데이터가 맞지 않습니다.");
  }

  private void checkSecretNumberInChatConnectCache(
      ChatConnectCache cache, StompHeaderAccessor accessor) {
    String secreteNumberHeader = String.valueOf(accessor.getNativeHeader("ChatRoomSecreteNumber"));
    if (secreteNumberHeader == null)
      throw new IncorrectChatDataInput("secreteNumberHeader 헤더에 대한 데이터가 존재하지 않습니다.");
    String secretNumber = secreteNumberHeader.substring(1, secreteNumberHeader.length() - 1);
    if (!cache.getSecreteNumber().equals(secretNumber))
      throw new IncorrectChatDataInput("secreteNumber가 올바르지 않습니다.");
  }
}
