package com.project.shoppingmall.controller_manage.root_manager.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputOnOffManagerMode {
  @NotNull private Boolean isOn;
}
