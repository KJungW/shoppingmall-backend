package com.project.shoppingmall.final_value;

public class CacheTemplate {
  private static final String CHAT_CONNECT_CACHE_KEY_PREFIX = "chat/connect/request/%s";

  public static String makeChatConnectCacheKey(long requesterId) {
    return String.format(CHAT_CONNECT_CACHE_KEY_PREFIX, requesterId);
  }
}
