package com.project.shoppingmall.controller.email;

import com.project.shoppingmall.controller.email.input.InputRegisterEmail;
import com.project.shoppingmall.controller.email.input.InputRequestEmailRegister;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.service.email.EmailRegistrationService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("email")
@RequiredArgsConstructor
public class EmailController {
  @Value("${frontend.address}")
  private String frontAddress;

  private final EmailRegistrationService emailRegistrationService;

  @PostMapping("/registration/request")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void requestEmailRegister(@Valid @RequestBody InputRequestEmailRegister input) {
    AuthUserDetail authUserDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    emailRegistrationService.sendCertificationEmail(authUserDetail.getId(), input.getEmail());
  }

  @GetMapping("/registration")
  public ResponseEntity<?> registerEmail(@Valid @ModelAttribute InputRegisterEmail input) {
    emailRegistrationService.registerEmail(
        input.getMemberId(), input.getCertificationNumber(), input.getEmail());

    String redirectUrlString = frontAddress + "/" + "mail/" + input.getMemberId();
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(redirectUrlString));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }
}
