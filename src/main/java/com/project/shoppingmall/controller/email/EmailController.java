package com.project.shoppingmall.controller.email;

import com.project.shoppingmall.controller.email.input.InputRegisterEmail;
import com.project.shoppingmall.controller.email.input.InputRequestEmailRegister;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.service.EmailRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("email")
@RequiredArgsConstructor
public class EmailController {
  private final EmailRegistrationService emailRegistrationService;

  @PostMapping("/registration/request")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void requestEmailRegister(@Valid @RequestBody InputRequestEmailRegister input) {
    AuthUserDetail authUserDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    emailRegistrationService.sendCertificationEmail(authUserDetail.getId(), input.getEmail());
  }

  @GetMapping("/registration")
  public String registerEmail(@Valid @ModelAttribute InputRegisterEmail input) {
    emailRegistrationService.registerEmail(
        input.getMemberId(), input.getCertificationNumber(), input.getEmail());
    return "이메일 등록이 완료되었습니다.";
  }
}
