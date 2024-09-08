package com.project.shoppingmall.final_value;

public class CacheTemplate {
  private static final String CHAT_CONNECT_CACHE_KEY_PREFIX = "chat/connect/request/%s";
  private static final String MEMBER_SIGNUP_BY_EMAIL_KEY_PREFIX = "member/signup/request/%s";
  private static final String EMAIL_REGISTER_CACHE_PREFIX = "member/email/%s";

  public static String makeChatConnectCacheKey(long requesterId) {
    return String.format(CHAT_CONNECT_CACHE_KEY_PREFIX, requesterId);
  }

  public static String makeMemberSignupByEmailKey(String email) {
    return String.format(MEMBER_SIGNUP_BY_EMAIL_KEY_PREFIX, email);
  }

  public static String makeEmailRegisterCacheKey(long memberId) {
    return String.format(EMAIL_REGISTER_CACHE_PREFIX, memberId);
  }
}
