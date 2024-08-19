package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.ManagerRoleType;
import com.project.shoppingmall.util.PasswordEncoderUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Manager extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String serialNumber;

  private String password;

  @Enumerated(EnumType.STRING)
  private ManagerRoleType role;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "TOKEN_ID")
  private ManagerToken token;

  @Builder
  public Manager(String serialNumber, String password, ManagerRoleType role) {
    if (serialNumber == null || serialNumber.isBlank())
      throw new ServerLogicError("Manager를 생성할때 비어있는 serialNumber값을 입력했습니다.");
    if (password == null || password.isBlank())
      throw new ServerLogicError("Manager를 생성할때 비어있는 password값을 입력했습니다.");
    if (role == null) throw new ServerLogicError("Manager를 생성할때 비어있는 role값을 입력했습니다.");
    this.serialNumber = serialNumber;
    this.password = PasswordEncoderUtil.encodePassword(password);
    this.role = role;
  }

  public void updateRefreshToken(ManagerToken token) {
    if (token == null || token.getRefresh() == null || token.getRefresh().isBlank())
      throw new ServerLogicError("Manger의 token필드에 비어있는 ManagerToken를 입력했습니다.");
    this.token = token;
  }

  public void deleteRefreshToken() {
    this.token = null;
  }
}
