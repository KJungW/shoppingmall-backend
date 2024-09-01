package com.project.shoppingmall.interceptor;

import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class ChatHandShakingProcessInterceptor implements HandshakeInterceptor {
  private final CacheRepository cacheRepository;

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes)
      throws Exception {
    UriComponents uriComp = UriComponentsBuilder.fromUri(request.getURI()).build();
    Map<String, String> queryParams = uriComp.getQueryParams().toSingleValueMap();
    try {
      long requesterId = Long.parseLong(queryParams.get("requesterId"));
      return cacheRepository.hasKey(CacheTemplate.makeChatConnectCacheKey(requesterId));
    } catch (Exception ex) {
      return false;
    }
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {}
}
