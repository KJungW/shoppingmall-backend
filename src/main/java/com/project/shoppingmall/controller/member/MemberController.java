package com.project.shoppingmall.controller.member;

import com.project.shoppingmall.controller.member.input.InputLoginByEmail;
import com.project.shoppingmall.controller.member.input.InputRequestSignup;
import com.project.shoppingmall.controller.member.input.InputUpdateMemberInfo;
import com.project.shoppingmall.controller.member.output.OutputGetMember;
import com.project.shoppingmall.controller.member.output.OutputLoginByEmail;
import com.project.shoppingmall.controller.member.output.OutputUpdateMemberInfo;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.dto.member.MemberEmailSignupDto;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.service.member.MemberDeleteService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.util.CookieUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
  private final MemberService memberService;
  private final MemberFindService memberFindService;
  private final MemberDeleteService memberDeleteService;
  private final CookieUtil cookieUtil;
  private final JwtUtil jwtUtil;

  @Value("${frontend.signup_by_email_success_url}")
  private String signupByEmailSuccessUrl;

  @Value("${frontend.signup_by_email_fail_url}")
  private String signupByEmailFailUrl;

  @PostMapping("/signup/request")
  public void requestSignupByEmail(@Valid @RequestBody InputRequestSignup input) {
    MemberEmailSignupDto signupDto =
        MemberEmailSignupDto.builder()
            .email(input.getEmail())
            .nickName(input.getNickName())
            .password(input.getPassword())
            .build();
    memberService.requestSignupByEmail(signupDto);
  }

  @GetMapping("/signup")
  public void signupByEmail(
      HttpServletResponse response,
      @RequestParam("email") String email,
      @RequestParam("secretNumber") String secretNumber) {
    try {
      Member member = memberService.signupByEmail(email, secretNumber);
      ResponseCookie cookie =
          cookieUtil.createCookie(
              "refresh",
              member.getToken().getRefresh(),
              (int) (jwtUtil.getRefreshExpirationTimeMs() / 1000));
      response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      response.sendRedirect(signupByEmailSuccessUrl);
    } catch (Exception ex) {
      try {
        response.sendRedirect(signupByEmailFailUrl);
      } catch (IOException e) {
        throw new ServerLogicError("예상치 못한 리다이렉션을 세팅하는 도중 예상치 못한 에러가 발생했습니다");
      }
    }
  }

  @PostMapping("/login")
  public OutputLoginByEmail loginByEmail(
      HttpServletResponse response, @Valid @RequestBody InputLoginByEmail input) {
    Member member = memberService.loginByEmail(input.getEmail(), input.getPassword());
    ResponseCookie cookie =
        cookieUtil.createCookie(
            "refresh",
            member.getToken().getRefresh(),
            (int) (jwtUtil.getRefreshExpirationTimeMs() / 1000));
    response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return new OutputLoginByEmail(member);
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputGetMember getMember() {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Member member =
        memberFindService
            .findById(userDetail.getId())
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 데이터가 존재하지 않습니다."));
    return new OutputGetMember(member);
  }

  @PostMapping("/info")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputUpdateMemberInfo updateMemberInfo(
      @Valid @ModelAttribute InputUpdateMemberInfo memberInfo) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Member member =
        memberService.updateMemberNickNameAndProfileImg(
            userDetail.getId(), memberInfo.getNickName(), memberInfo.getProfileImg());
    return new OutputUpdateMemberInfo(member);
  }

  @DeleteMapping
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void deleteMember() {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    memberDeleteService.deleteMemberInController(userDetail.getId());
  }
}
