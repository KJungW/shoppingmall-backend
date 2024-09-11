package com.project.shoppingmall.service.member;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.type.LoginType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberFindService {
  private final MemberRepository memberRepository;

  public Optional<Member> findById(Long memberId) {
    return memberRepository.findById(memberId);
  }

  public Optional<Member> findByLonginTypeAndSocialId(LoginType loginType, String socialID) {
    return memberRepository.findByLoginTypeAndSocialId(loginType, socialID);
  }

  public Optional<Member> findByEmail(String email) {
    return memberRepository.findByEmail(email);
  }

  public List<Member> findAllByIds(List<Long> memberIds) {
    return memberRepository.findAllById(memberIds);
  }
}
