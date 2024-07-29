package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.cache.EmailRegistrationCache;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.EmailRegistrationCacheError;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.RandomNumberGenerator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class EmailRegistrationServiceTest {
  private EmailRegistrationService target;
  private CacheRepository mockCacheRepository;
  private RandomNumberGenerator mockRandomNumberGenerator;
  private EmailService mockEmailService;
  private MemberService mockMemberService;
  private JsonUtil jsonUtil;
  private Long expirationTime;
  private String domain;
  private String cacheName;

  @BeforeEach
  public void beforeEach() {
    mockCacheRepository = mock(CacheRepository.class);
    mockRandomNumberGenerator = mock(RandomNumberGenerator.class);
    mockEmailService = mock(EmailService.class);
    mockMemberService = mock(MemberService.class);
    jsonUtil = new JsonUtil();
    target =
        new EmailRegistrationService(
            mockCacheRepository,
            mockRandomNumberGenerator,
            mockEmailService,
            mockMemberService,
            jsonUtil);

    expirationTime = 180L;
    domain = "https://domain";
    ReflectionTestUtils.setField(target, "expirationTime", expirationTime);
    ReflectionTestUtils.setField(target, "domain", domain);
    cacheName = ReflectionTestUtils.getField(target, "cacheName").toString();
  }

  @Test
  @DisplayName("sendCertificationEmail() : 정상흐름")
  public void sendCertificationEmail_ok() {
    // given
    Long givenMemberId = 1L;
    String givenEmail = "test@test.com";
    String givenRandomNum = "010101";
    EmailRegistrationCache givenEmailRegistrationCache =
        new EmailRegistrationCache(givenMemberId, givenRandomNum);
    String givenCacheJson = jsonUtil.convertObjectToJson(givenEmailRegistrationCache);
    String expectedContent =
        String.format(
            "%s/email/registration?memberId=%s&certificationNumber=%s&email=%s  (3분 이내에 클릭)",
            domain, givenMemberId.toString(), givenRandomNum, givenEmail);
    when(mockRandomNumberGenerator.makeRandomNumber()).thenReturn(givenRandomNum);

    // when
    target.sendCertificationEmail(givenMemberId, givenEmail);

    // then
    // 저장된 캐시내용 검증
    ArgumentCaptor<String> argEmail = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> argCacheJson = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Long> argExp = ArgumentCaptor.forClass(Long.class);
    verify(mockCacheRepository, times(1))
        .saveCache(argEmail.capture(), argCacheJson.capture(), argExp.capture());
    assertEquals(cacheName + givenEmail, argEmail.getValue());
    assertEquals(givenCacheJson, argCacheJson.getValue());
    assertEquals(expirationTime, argExp.getValue());

    // 전송된 메일 내용 검증
    ArgumentCaptor<String> argEmail2 = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> argContent = ArgumentCaptor.forClass(String.class);
    verify(mockEmailService, times(1)).sendMail(argEmail2.capture(), any(), argContent.capture());
    assertEquals(givenEmail, argEmail2.getValue());
    assertEquals(expectedContent, argContent.getValue());
  }

  @Test
  @DisplayName("registerEmail() : 정상흐름")
  public void registerEmail_ok() {
    // given
    Long givenMemberId = 1L;
    String givenCertificationNumber = "010101";
    String givenEmail = "test@test.com";
    EmailRegistrationCache givenEmailRegistrationCache =
        new EmailRegistrationCache(givenMemberId, givenCertificationNumber);
    String givenCacheJson = jsonUtil.convertObjectToJson(givenEmailRegistrationCache);
    Member givenMember = MemberBuilder.fullData().email(null).build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);

    when(mockCacheRepository.getCache(any())).thenReturn(Optional.of(givenCacheJson));
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // when
    target.registerEmail(givenMemberId, givenCertificationNumber, givenEmail);

    // then
    // 저장된 캐시 조회 검증
    ArgumentCaptor<String> argKey = ArgumentCaptor.forClass(String.class);
    verify(mockCacheRepository, times(1)).getCache(argKey.capture());
    assertEquals(cacheName + givenEmail, argKey.getValue());

    // 저장된 캐시 제거 검증
    ArgumentCaptor<String> argKey2 = ArgumentCaptor.forClass(String.class);
    verify(mockCacheRepository, times(1)).removeCache(argKey2.capture());
    assertEquals(cacheName + givenEmail, argKey2.getValue());

    // 멤버에 이메일 등록 검증
    ArgumentCaptor<Member> member = ArgumentCaptor.forClass(Member.class);
    assertEquals(givenEmail, givenMember.getEmail());
  }

  @Test
  @DisplayName("registerEmail() : 잘못된 인증번호로 인한 인증불가")
  public void registerEmail_WrongCertificationNumber() {
    // given
    Long givenMemberId = 1L;
    String givenRightCertificationNumber = "111111";
    String givenWrongCertificationNumber = "222222";
    String givenEmail = "test@test.com";
    EmailRegistrationCache givenCache =
        new EmailRegistrationCache(givenMemberId, givenRightCertificationNumber);
    String givenCacheJson = jsonUtil.convertObjectToJson(givenCache);
    when(mockCacheRepository.getCache(any())).thenReturn(Optional.of(givenCacheJson));

    // when then
    assertThrows(
        EmailRegistrationCacheError.class,
        () -> target.registerEmail(givenMemberId, givenWrongCertificationNumber, givenEmail));
  }

  @Test
  @DisplayName("registerEmail() : 잘못된 회원ID에 의한 인증불가")
  public void registerEmail_WrongMemberId() {
    // given
    Long givenMemberId = 1L;
    Long wrongMemberId = 2L;
    String givenRightCertificationNumber = "111111";
    String givenEmail = "test@test.com";
    EmailRegistrationCache givenCache =
        new EmailRegistrationCache(givenMemberId, givenRightCertificationNumber);
    String givenCacheJson = jsonUtil.convertObjectToJson(givenCache);
    when(mockCacheRepository.getCache(any())).thenReturn(Optional.of(givenCacheJson));

    // when then
    assertThrows(
        EmailRegistrationCacheError.class,
        () -> target.registerEmail(wrongMemberId, givenRightCertificationNumber, givenEmail));
  }

  @Test
  @DisplayName("registerEmail() : 캐시가 존재하지 않음")
  public void registerEmail_NoCache() {
    // given
    Long givenMemberId = 1L;
    String givenCertificationNumber = "111111";
    String givenEmail = "test@test.com";
    when(mockCacheRepository.getCache(any())).thenReturn(Optional.empty());

    // when then
    assertThrows(
        EmailRegistrationCacheError.class,
        () -> target.registerEmail(givenMemberId, givenCertificationNumber, givenEmail));
  }
}
