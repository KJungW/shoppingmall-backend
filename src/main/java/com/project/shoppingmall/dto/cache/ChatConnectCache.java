package com.project.shoppingmall.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatConnectCache {
  private Long requesterId;
  private Long chatRoomId;
  private String secreteNumber;
}
