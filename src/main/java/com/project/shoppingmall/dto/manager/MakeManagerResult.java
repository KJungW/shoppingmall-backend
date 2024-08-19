package com.project.shoppingmall.dto.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MakeManagerResult {
  private Long createdManagerId;
  private String serialNumber;
  private String password;
}
