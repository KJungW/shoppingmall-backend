package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.type.LoginType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByLoginTypeAndSocialId(LoginType loginType, String socialId);

  Optional<Member> findByEmail(String email);
}
