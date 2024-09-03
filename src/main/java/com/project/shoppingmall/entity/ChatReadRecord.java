package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"chatRoom", "member"})})
public class ChatReadRecord extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  private String latestReadMessageId;

  @Builder
  public ChatReadRecord(ChatRoom chatRoom, Member member) {
    if (chatRoom == null)
      throw new ServerLogicError("ChatReadRecord의 chatRoom 필드에 비어있는 값이 입력되었습니다.");
    if (member == null) throw new ServerLogicError("ChatReadRecord의 member 필드에 비어있는 값이 입력되었습니다.");
    this.chatRoom = chatRoom;
    this.member = member;
  }

  public void updateLatestReadMessageId(String latestReadMessageId) {
    if (latestReadMessageId == null || latestReadMessageId.isBlank())
      throw new ServerLogicError("ChatReadRecord의 latestReadMessageId 필드에 비어있는 값이 입력되었습니다.");
    this.latestReadMessageId = latestReadMessageId;
  }
}
