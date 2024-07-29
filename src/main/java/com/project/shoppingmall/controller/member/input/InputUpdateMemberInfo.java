package com.project.shoppingmall.controller.member.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class InputUpdateMemberInfo {
  @NotBlank
  @Length(min = 5, max = 15)
  private String nickName;

  @NotNull private MultipartFile profileImg;
}
