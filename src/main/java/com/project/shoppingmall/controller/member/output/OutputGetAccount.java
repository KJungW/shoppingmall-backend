package com.project.shoppingmall.controller.member.output;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputGetAccount {
  private Long memberId;
  private String accountNumber;
}
