package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.cache.EmailRegistrationCache;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.EmailRegistrationCacheError;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.service.email.EmailRegistrationService;
import com.project.shoppingmall.service.email.EmailService;
import com.project.shoppingmall.service.member.MemberService;
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
  private Long expirationTime;
  private String domain;

  @BeforeEach
  public void beforeEach() {
    mockCacheRepository = mock(CacheRepository.class);
    mockRandomNumberGenerator = mock(RandomNumberGenerator.class);
    mockEmailService = mock(EmailService.class);
    mockMemberService = mock(MemberService.class);
    target =
        new EmailRegistrationService(
            mockCacheRepository, mockRandomNumberGenerator, mockEmailService, mockMemberService);

    expirationTime = 180L;
    domain = "https://domain";
    ReflectionTestUtils.setField(target, "expirationTime", expirationTime);
    ReflectionTestUtils.setField(target, "domain", domain);
  }

  @Test
  @DisplayName("sendCertificationEmail() : 정상흐름")
  public void sendCertificationEmail_ok() {
    // given
    Long inputMemberId = 1L;
    String inputEmail = "test@test.com";

    String givenCertificationNumber = "010101";
    when(mockMemberService.findByEmail(anyString())).thenReturn(Optional.empty());
    when(mockRandomNumberGenerator.makeRandomNumber()).thenReturn(givenCertificationNumber);

    // when
    target.sendCertificationEmail(inputMemberId, inputEmail);

    // then
    check_cacheRepository_saveCache(inputMemberId, givenCertificationNumber);
    check_emailService_sendMail(inputMemberId, inputEmail, givenCertificationNumber);
  }

  @Test
  @DisplayName("registerEmail() : 정상흐름")
  public void registerEmail_ok() {
    // given
    Long inputMemberId = 1L;
    String inputCertificationNumber = "010101";
    String inputEmail = "test@test.com";

    Member givenMember = setMember(inputMemberId);
    set_mockCacheRepository_getCache(inputMemberId, inputCertificationNumber);
    set_memberService_findById(givenMember);
    set_memberService_findByEmail();

    // when
    target.registerEmail(inputMemberId, inputCertificationNumber, inputEmail);

    // then
    check_cacheRepository_removeCache(inputMemberId);
    assertEquals(inputEmail, givenMember.getEmail());
  }

  @Test
  @DisplayName("registerEmail() : 캐시가 존재하지 않음")
  public void registerEmail_NoCache() {
    // given
    Long inputMemberId = 1L;
    String inputCertificationNumber = "010101";
    String inputEmail = "test@test.com";

    Member givenMember = setMember(inputMemberId);
    set_mockCacheRepository_getCache();
    set_memberService_findById(givenMember);
    set_memberService_findByEmail();

    // when then
    assertThrows(
        EmailRegistrationCacheError.class,
        () -> target.registerEmail(inputMemberId, inputCertificationNumber, inputEmail));
  }

  @Test
  @DisplayName("registerEmail() : 잘못된 회원ID에 의한 인증불가")
  public void registerEmail_WrongMemberId() {
    // given
    Long inputMemberId = 1L;
    String inputCertificationNumber = "010101";
    String inputEmail = "test@test.com";

    set_mockCacheRepository_getCache(inputMemberId, inputCertificationNumber);
    set_memberService_findById();
    set_memberService_findByEmail();

    // when then
    assertThrows(
        DataNotFound.class,
        () -> target.registerEmail(inputMemberId, inputCertificationNumber, inputEmail));
  }

  @Test
  @DisplayName("registerEmail() : 중복된 이메일을 등록할 경우")
  public void registerEmail_duplicateCache() {
    // given
    Long inputMemberId = 1L;
    String inputCertificationNumber = "010101";
    String inputEmail = "test@test.com";

    Member givenMember = setMember(inputMemberId);
    Member duplicateEmailMember = setMember(30L, inputEmail);
    set_mockCacheRepository_getCache();
    set_memberService_findById(givenMember);
    set_memberService_findByEmail(duplicateEmailMember);

    // when then
    assertThrows(
        EmailRegistrationCacheError.class,
        () -> target.registerEmail(inputMemberId, inputCertificationNumber, inputEmail));
  }

  @Test
  @DisplayName("registerEmail() : 잘못된 인증번호로 인한 인증불가")
  public void registerEmail_wrongCertificationNumber() {
    // given
    Long inputMemberId = 1L;
    String inputCertificationNumber = "010101";
    String inputEmail = "test@test.com";

    String otherCertificationNumber = "210312";

    Member givenMember = setMember(inputMemberId);
    set_mockCacheRepository_getCache(inputMemberId, otherCertificationNumber);
    set_memberService_findById(givenMember);
    set_memberService_findByEmail();

    // when then
    assertThrows(
        EmailRegistrationCacheError.class,
        () -> target.registerEmail(inputMemberId, inputCertificationNumber, inputEmail));
  }

  public Member setMember(long givenMemberId) {
    Member givenMember = MemberBuilder.fullData().email(null).build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    return givenMember;
  }

  public Member setMember(long givenMemberId, String givenEmail) {
    Member givenMember = MemberBuilder.fullData().email(givenEmail).build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    return givenMember;
  }

  public void set_memberService_findById(Member givenMember) {
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));
  }

  public void set_memberService_findById() {
    when(mockMemberService.findById(any())).thenReturn(Optional.empty());
  }

  public void set_memberService_findByEmail(Member givenMember) {
    when(mockMemberService.findByEmail(any())).thenReturn(Optional.of(givenMember));
  }

  public void set_memberService_findByEmail() {
    when(mockMemberService.findByEmail(any())).thenReturn(Optional.empty());
  }

  public void set_mockCacheRepository_getCache(
      long givenMemberId, String givenCertificationNumber) {
    EmailRegistrationCache givenEmailRegistrationCache =
        new EmailRegistrationCache(givenMemberId, givenCertificationNumber);
    String givenCacheJson = JsonUtil.convertObjectToJson(givenEmailRegistrationCache);
    when(mockCacheRepository.getCache(any())).thenReturn(Optional.of(givenCacheJson));
  }

  public void set_mockCacheRepository_getCache() {
    when(mockCacheRepository.getCache(any())).thenReturn(Optional.empty());
  }

  public void check_cacheRepository_saveCache(long givenMemberId, String givenRandomNum) {
    ArgumentCaptor<String> cacheKeyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> cacheValueCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Long> expirationTimeCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockCacheRepository, times(1))
        .saveCache(
            cacheKeyCaptor.capture(), cacheValueCaptor.capture(), expirationTimeCaptor.capture());

    String expectedCacheKey = CacheTemplate.makeEmailRegisterCacheKey(givenMemberId);
    String realCacheKey = cacheKeyCaptor.getValue();
    assertEquals(expectedCacheKey, realCacheKey);

    String expectedCacheValue =
        JsonUtil.convertObjectToJson(new EmailRegistrationCache(givenMemberId, givenRandomNum));
    String realCacheValue = cacheValueCaptor.getValue();
    assertEquals(expectedCacheValue, realCacheValue);
    assertEquals(expirationTime, expirationTimeCaptor.getValue());
  }

  public void check_cacheRepository_removeCache(long givenMemberId) {
    ArgumentCaptor<String> cacheKeyCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockCacheRepository, times(1)).removeCache(cacheKeyCaptor.capture());
    String expectedCacheKey = CacheTemplate.makeEmailRegisterCacheKey(givenMemberId);
    String realCacheKey = cacheKeyCaptor.getValue();
    assertEquals(expectedCacheKey, realCacheKey);
  }

  public void check_emailService_sendMail(
      long givenMemberId, String givenEmail, String inputCertificationNumber) {
    ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> emailTitleCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> emailContentCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockEmailService, times(1))
        .sendMail(emailCaptor.capture(), emailTitleCaptor.capture(), emailContentCaptor.capture());

    assertEquals(givenEmail, emailCaptor.getValue());
    assertEquals("[ShopingMall] 인증메일", emailTitleCaptor.getValue());

    String expectedEmailContent =
        String.format(
            "%s/email/registration?memberId=%s&certificationNumber=%s&email=%s  (3분 이내에 클릭)",
            domain, givenMemberId, inputCertificationNumber, givenEmail);
    String realEmailContent = emailContentCaptor.getValue();
    assertEquals(expectedEmailContent, realEmailContent);
  }
}
