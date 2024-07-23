package com.project.shoppingmall.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberToken {
  @Id @GeneratedValue private Long id;
  private String refresh;

  public MemberToken(String refresh) {
    this.refresh = refresh;
  }
}
