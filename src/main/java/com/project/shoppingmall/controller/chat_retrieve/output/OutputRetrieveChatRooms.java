package com.project.shoppingmall.controller.chat_retrieve.output;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.chat.ChatRoomDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputRetrieveChatRooms {
  private int currentSliceNumber;
  private int sliceSize;
  private boolean isFirst;
  private boolean isLast;
  private boolean hasNext;
  private boolean hasPrevious;
  private List<ChatRoomDto> chatRoomList;

  public OutputRetrieveChatRooms(SliceResult<ChatRoomDto> sliceResult) {
    this.currentSliceNumber = sliceResult.getCurrentSliceNumber();
    this.sliceSize = sliceResult.getSliceSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.isHasNext();
    this.hasPrevious = sliceResult.isHasPrevious();
    this.chatRoomList = sliceResult.getContentList();
  }
}
