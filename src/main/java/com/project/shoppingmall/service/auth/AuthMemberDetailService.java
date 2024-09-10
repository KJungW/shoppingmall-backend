package com.project.shoppingmall.service.auth;

import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.member.MemberFindService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthMemberDetailService implements UserDetailsService {
  private final MemberFindService memberFindService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Long id = Long.valueOf(username);
    Member member =
        memberFindService
            .findById(id)
            .orElseThrow(() -> new DataNotFound("현재 ID에 해당하는 사용자가 존재하지 않습니다."));
    return new AuthMemberDetail(member.getId(), member.getRole());
  }
}
