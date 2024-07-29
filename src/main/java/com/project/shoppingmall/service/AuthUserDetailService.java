package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthUserDetailService implements UserDetailsService {
  private final MemberService memberService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Long id = Long.valueOf(username);
    Member member =
        memberService
            .findById(id)
            .orElseThrow(() -> new DataNotFound("현재 ID에 해당하는 사용자가 존재하지 않습니다."));
    return new AuthUserDetail(member.getId(), member.getRole());
  }
}
