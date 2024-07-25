package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.type.LoginType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
  private final MemberRepository memberRepository;

  @Transactional
  public Member save(Member member) {
    return memberRepository.save(member);
  }

  public Optional<Member> findById(Long memberId) {
    return memberRepository.findById(memberId);
  }

  public Optional<Member> findByLonginTypeAndSocialId(LoginType loginType, String socialID) {
    return memberRepository.findByLoginTypeAndSocialId(loginType, socialID);
  }
}
