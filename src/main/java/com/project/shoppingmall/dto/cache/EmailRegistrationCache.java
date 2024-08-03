package com.project.shoppingmall.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailRegistrationCache {
  private Long memberId;
  private String certificationNumber;

  public Boolean checkIsEquals(EmailRegistrationCache target) {
    return this.memberId.equals(target.getMemberId())
        && this.certificationNumber.equals(target.getCertificationNumber());
  }
}
