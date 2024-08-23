package com.project.shoppingmall.controller.member;

import com.project.shoppingmall.controller.member.input.InputUpdateMemberInfo;
import com.project.shoppingmall.controller.member.output.OutputGetMember;
import com.project.shoppingmall.controller.member.output.OutputUpdateMemberInfo;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.member.MemberService;
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

  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputGetMember getMember() {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Member member =
        memberService
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
}
