package com.project.shoppingmall.controller.member.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class InputUpdateMemberInfo {
  @NotBlank private String nickName;
  @NotNull private MultipartFile profileImg;
}
