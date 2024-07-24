package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.type.LoginType;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class MemberServiceTest {
  private MemberService memberService;
  private MemberRepository memberRepository;

  @BeforeEach
  public void beforeEach() {
    memberRepository = Mockito.mock(MemberRepository.class);
    memberService = new MemberService(memberRepository);
  }

  @Test
  @DisplayName("MemberService.save() : 정상흐름")
  public void save_ok() {
    // given
    Member givenMember = MemberBuilder.fullData().build();
    Long givenMemberId = 1L;

    Member memberRepositorySaveResult = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(memberRepositorySaveResult, "id", givenMemberId);
    Mockito.when(memberRepository.save(Mockito.any())).thenReturn(memberRepositorySaveResult);

    // when
    Member resultMember = memberService.save(givenMember);

    // then
    Mockito.verify(memberRepository, Mockito.times(1)).save(Mockito.any());
    Member expectedMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(expectedMember, "id", givenMemberId);

    Assertions.assertEquals(expectedMember.getId(), resultMember.getId());
    Assertions.assertEquals(expectedMember.getLoginType(), resultMember.getLoginType());
    Assertions.assertEquals(expectedMember.getLoginType(), resultMember.getLoginType());
    Assertions.assertEquals(expectedMember.getSocialId(), resultMember.getSocialId());
    Assertions.assertEquals(expectedMember.getNickName(), resultMember.getNickName());
    Assertions.assertEquals(expectedMember.getEmail(), resultMember.getEmail());
    Assertions.assertEquals(expectedMember.getProfileImageUrl(), resultMember.getProfileImageUrl());
    Assertions.assertEquals(expectedMember.getRole(), resultMember.getRole());
    Assertions.assertEquals(expectedMember.getIsBan(), resultMember.getIsBan());
    Assertions.assertEquals(
        expectedMember.getToken().getRefresh(), resultMember.getToken().getRefresh());
  }

  @Test
  @DisplayName("MemberService.findByLonginTypeAndSocialId() : 정상흐름")
  public void findByLonginTypeAndSocialId_ok() {
    // given
    LoginType givenLoginType = LoginType.NAVER;
    String givenSocialId = "lskdfjkldsj123421351";
    Mockito.when(memberRepository.findByLoginTypeAndSocialId(Mockito.any(), Mockito.any()))
        .thenReturn(
            Optional.of(
                MemberBuilder.fullData()
                    .loginType(givenLoginType)
                    .socialId(givenSocialId)
                    .build()));

    // when
    Optional<Member> result =
        memberService.findByLonginTypeAndSocialId(givenLoginType, givenSocialId);

    // then
    Assertions.assertTrue(result.isPresent());
    Mockito.verify(memberRepository, Mockito.times(1))
        .findByLoginTypeAndSocialId(givenLoginType, givenSocialId);

    Member resultMember = result.get();
    Assertions.assertEquals(givenLoginType, resultMember.getLoginType());
    Assertions.assertEquals(givenSocialId, resultMember.getSocialId());
  }

  @Test
  @DisplayName("MemberService.findByLonginTypeAndSocialId() : 조회데이터 없음")
  public void findByLonginTypeAndSocialId_notData() {
    // given
    LoginType givenLoginType = LoginType.NAVER;
    String givenSocialId = "lskdfjkldsj123421351";
    Mockito.when(memberRepository.findByLoginTypeAndSocialId(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());

    // when
    Optional<Member> result =
        memberService.findByLonginTypeAndSocialId(givenLoginType, givenSocialId);

    // then
    Assertions.assertFalse(result.isPresent());
    Mockito.verify(memberRepository, Mockito.times(1))
        .findByLoginTypeAndSocialId(givenLoginType, givenSocialId);
  }
}
