package com.project.shoppingmall.controller.member;

import com.project.shoppingmall.controller.member.input.InputUpdateMemberInfo;
import com.project.shoppingmall.controller.member.input.OutputUpdateMemberInfo;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
  private final MemberService memberService;

  @PostMapping("/info")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputUpdateMemberInfo updateMemberInfo(
      @Valid @ModelAttribute InputUpdateMemberInfo memberInfo) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Member member =
        memberService.updateMemberNickNameAndProfileImg(
            userDetail.getId(), memberInfo.getNickName(), memberInfo.getProfileImg());
    return new OutputUpdateMemberInfo(member);
  }
}
