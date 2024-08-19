package com.project.shoppingmall.controller.root_manager.output;

import com.project.shoppingmall.dto.manager.MakeManagerResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputMakeCommonManger {
  private Long createdManagerId;
  private String serialNumber;
  private String password;

  public OutputMakeCommonManger(MakeManagerResult makeManagerResult) {
    this.createdManagerId = makeManagerResult.getCreatedManagerId();
    this.serialNumber = makeManagerResult.getSerialNumber();
    this.password = makeManagerResult.getPassword();
  }
}
