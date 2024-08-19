package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManagerToken extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String refresh;

  public ManagerToken(String refresh) {
    updateRefresh(refresh);
  }

  public void updateRefresh(String refresh) {
    if (refresh == null || refresh.isBlank())
      throw new ServerLogicError("ManagerToken.refresh에 비어있는 값이 입력되었습니다.");
    this.refresh = refresh;
  }
}
