package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.cache.MemberSignupByEmailCache;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.member.MemberEmailSignupDto;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.MemberToken;
import com.project.shoppingmall.exception.DuplicateMemberEmail;
import com.project.shoppingmall.exception.MemberSignupByEmailCacheError;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.service.email.EmailService;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.JwtUtil;
import com.project.shoppingmall.util.PasswordEncoderUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

class MemberServiceTest {
  private MemberService target;
  private MemberRepository mockMemberRepository;
  private CacheRepository mockCacheRepository;
  private S3Service mockS3Service;
  private EmailService mockEmailService;
  private JwtUtil mockJwtUtil;
  private Long mockMemberSignupRequestExpirationTime;
  private String mockDomain;

  @BeforeEach
  public void beforeEach() {
    mockMemberSignupRequestExpirationTime = 180L;
    mockDomain = "http://localhost:8080";

    mockMemberRepository = mock(MemberRepository.class);
    mockCacheRepository = mock(CacheRepository.class);
    mockS3Service = mock(S3Service.class);
    mockEmailService = mock(EmailService.class);
    mockJwtUtil = mock(JwtUtil.class);

    target =
        new MemberService(
            mockMemberRepository,
            mockCacheRepository,
            mockS3Service,
            mockEmailService,
            mockJwtUtil);
    ReflectionTestUtils.setField(
        target, "memberSignupRequestExpirationTime", mockMemberSignupRequestExpirationTime);
    ReflectionTestUtils.setField(target, "domain", mockDomain);
  }

  @Test
  @DisplayName("requestSignupByEmail() : 정상흐름")
  public void requestSignupByEmail_ok() {
    UUID givenSecretNumber = UUID.randomUUID();
    MockedStatic<UUID> mockUuid = mockStatic(UUID.class);

    // given
    MemberEmailSignupDto inputDto =
        new MemberEmailSignupDto("example@temp.com", "ckkqkes1231254", "tempNickName");

    when(mockMemberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(UUID.randomUUID()).thenReturn(givenSecretNumber);

    // when
    target.requestSignupByEmail(inputDto);

    // then
    check_cacheRepository_saveCache(inputDto, givenSecretNumber.toString());
    check_emailService_sendMail(inputDto.getEmail(), givenSecretNumber.toString());

    mockUuid.close();
  }

  @Test
  @DisplayName("requestSignupByEmail() : 중복 이메일을 가지고 회원가입을 요청할 경우")
  public void requestSignupByEmail_duplicateEmail() {
    UUID givenSecretNumber = UUID.randomUUID();
    MockedStatic<UUID> mockUuid = mockStatic(UUID.class);

    // given
    MemberEmailSignupDto inputDto =
        new MemberEmailSignupDto("example@temp.com", "ckkqkes1231254", "tempNickName");

    Member duplicateEmailMember = MemberBuilder.fullData().email(inputDto.getEmail()).build();
    when(mockMemberRepository.findByEmail(anyString()))
        .thenReturn(Optional.of(duplicateEmailMember));
    when(UUID.randomUUID()).thenReturn(givenSecretNumber);

    // when then
    assertThrows(DuplicateMemberEmail.class, () -> target.requestSignupByEmail(inputDto));

    mockUuid.close();
  }

  @Test
  @DisplayName("signupByEmail() : 정상흐름")
  public void signupByEmail_ok() {
    // given
    String inputEmail = "example@temp.com";
    String inputSecretNumber = "kalfwr1235cxzvewr";

    long givenMemberId = 10L;
    String givenRefreshTokenValue = "tempRefreshToken";

    Member givenMember = setMember(givenMemberId, inputSecretNumber);
    setMemberToken(givenMember, givenRefreshTokenValue);
    String givenCacheJson = setMemberSignupByEmailCache(givenMember, inputSecretNumber);
    when(mockCacheRepository.getCache(anyString())).thenReturn(Optional.of(givenCacheJson));
    when(mockMemberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshTokenValue);

    // when
    Member resultMember = target.signupByEmail(inputEmail, inputSecretNumber);

    // then
    checkMember(givenMember, resultMember);
    checkMemberToken(givenMember.getToken(), resultMember.getToken());
    check_memberRepository_save(givenMember);
    check_cacheRepository_removeCache(inputEmail);
  }

  @Test
  @DisplayName("signupByEmail() : 회원가입을 위한 인증 캐시가 존재하지 않는 경우")
  public void signupByEmail_notCache() {
    // given
    String inputEmail = "example@temp.com";
    String inputSecretNumber = "kalfwr1235cxzvewr";
    when(mockCacheRepository.getCache(anyString())).thenReturn(Optional.empty());

    // when then
    assertThrows(
        MemberSignupByEmailCacheError.class,
        () -> target.signupByEmail(inputEmail, inputSecretNumber));
  }

  @Test
  @DisplayName("signupByEmail() : 인증번호가 맞지 않는 경우")
  public void signupByEmail_incorrectSecretNumber() {
    // given
    String inputEmail = "example@temp.com";
    String inputSecretNumber = "kalfwr1235cxzvewr";
    String wrongSecretKey = "slkfjewlra";

    long givenMemberId = 10L;
    String givenRefreshTokenValue = "tempRefreshToken";

    Member givenMember = setMember(givenMemberId, inputSecretNumber);
    setMemberToken(givenMember, givenRefreshTokenValue);
    String givenCacheJson = setMemberSignupByEmailCache(givenMember, wrongSecretKey);
    when(mockCacheRepository.getCache(anyString())).thenReturn(Optional.of(givenCacheJson));
    when(mockMemberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshTokenValue);

    // when then
    assertThrows(
        MemberSignupByEmailCacheError.class,
        () -> target.signupByEmail(inputEmail, inputSecretNumber));
  }

  @Test
  @DisplayName("signupByEmail() : 등록할 회원의 이메일이 중복인 경우")
  public void signupByEmail_duplicateEmail() {
    // given
    String inputEmail = "example@temp.com";
    String inputSecretNumber = "kalfwr1235cxzvewr";

    long givenMemberId = 10L;
    String givenRefreshTokenValue = "tempRefreshToken";

    Member givenMember = setMember(givenMemberId, inputSecretNumber);
    setMemberToken(givenMember, givenRefreshTokenValue);
    String givenCacheJson = setMemberSignupByEmailCache(givenMember, inputSecretNumber);
    when(mockCacheRepository.getCache(anyString())).thenReturn(Optional.of(givenCacheJson));

    Member otherMember = MemberBuilder.fullData().build();
    when(mockMemberRepository.findByEmail(anyString())).thenReturn(Optional.of(otherMember));

    // when then
    assertThrows(
        DuplicateMemberEmail.class, () -> target.signupByEmail(inputEmail, inputSecretNumber));
  }

  @Test
  @DisplayName("MemberService.findByLonginTypeAndSocialId() : 정상흐름")
  public void findByLonginTypeAndSocialId_ok() {
    // given
    LoginType givenLoginType = LoginType.NAVER;
    String givenSocialId = "lskdfjkldsj123421351";
    when(mockMemberRepository.findByLoginTypeAndSocialId(any(), any()))
        .thenReturn(
            Optional.of(
                MemberBuilder.fullData()
                    .loginType(givenLoginType)
                    .socialId(givenSocialId)
                    .build()));

    // when
    Optional<Member> result = target.findByLonginTypeAndSocialId(givenLoginType, givenSocialId);

    // then
    assertTrue(result.isPresent());
    verify(mockMemberRepository, times(1))
        .findByLoginTypeAndSocialId(givenLoginType, givenSocialId);

    Member resultMember = result.get();
    assertEquals(givenLoginType, resultMember.getLoginType());
    assertEquals(givenSocialId, resultMember.getSocialId());
  }

  @Test
  @DisplayName("MemberService.findByLonginTypeAndSocialId() : 조회데이터 없음")
  public void findByLonginTypeAndSocialId_notData() {
    // given
    LoginType givenLoginType = LoginType.NAVER;
    String givenSocialId = "lskdfjkldsj123421351";
    when(mockMemberRepository.findByLoginTypeAndSocialId(any(), any()))
        .thenReturn(Optional.empty());

    // when
    Optional<Member> result = target.findByLonginTypeAndSocialId(givenLoginType, givenSocialId);

    // then
    assertFalse(result.isPresent());
    verify(mockMemberRepository, times(1))
        .findByLoginTypeAndSocialId(givenLoginType, givenSocialId);
  }

  @Test
  @DisplayName("MemberService.updateMemberNickNameAndProfileImg() : 정상 흐름")
  public void updateMemberNickNameAndProfileImg_ok() throws IOException {
    // given
    Long givenMemberId = 1L;
    String givenNickname = "testNickname";
    MultipartFile givenProfileImage =
        new MockMultipartFile(
            "profileSampleImage.png",
            new FileInputStream(new ClassPathResource("static/profileSampleImage.png").getFile()));
    Member givenMember =
        MemberBuilder.fullData()
            .nickName("kim")
            .profileImageUrl(null)
            .profileImageDownLoadUrl(null)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    String givenServerUri = "testServerUri";
    String givenDownloadUrl = "testDownLoadUrl";

    when(mockMemberRepository.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockS3Service.uploadFile(any(), any()))
        .thenReturn(new FileUploadResult(givenServerUri, givenDownloadUrl));

    // when
    Member member =
        target.updateMemberNickNameAndProfileImg(givenMemberId, givenNickname, givenProfileImage);

    // then
    // 기존에 저장된 프로필이미지 데이터가 없으므로, S3Service.deleteFile()가 실행되지 않아야한다.
    verify(mockS3Service, times(0)).deleteFile(any());

    // S3Service.uploadFile() 메서드 인자 검증
    ArgumentCaptor<MultipartFile> multipartFileArgumentCaptor =
        ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> directoryPathCapture = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1))
        .uploadFile(multipartFileArgumentCaptor.capture(), directoryPathCapture.capture());
    assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());
    assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());

    // 회원정보 수정 결과 검증
    assertEquals(givenNickname, member.getNickName());
    assertEquals(givenServerUri, member.getProfileImageUrl());
    assertEquals(givenDownloadUrl, member.getProfileImageDownLoadUrl());
  }

  @Test
  @DisplayName("MemberService.updateMemberNickNameAndProfileImg() : 기존 데이터 덮어씌우기")
  public void updateMemberNickNameAndProfileImg_dataOverwrite() throws IOException {
    // given
    Long givenMemberId = 1L;
    String originNickName = "originNickname";
    String originServerUri = "originServerUri";
    String originDownloadUrl = "originDownloadUrl";
    String givenNickname = "testNickname";
    String givenServerUri = "testServerUri";
    String givenDownloadUrl = "testDownLoadUrl";
    MultipartFile givenProfileImage =
        new MockMultipartFile(
            "profileSampleImage.png",
            new FileInputStream(new ClassPathResource("static/profileSampleImage.png").getFile()));
    Member givenMember =
        MemberBuilder.fullData()
            .nickName(originNickName)
            .profileImageUrl(originServerUri)
            .profileImageDownLoadUrl(originDownloadUrl)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);

    when(mockMemberRepository.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockS3Service.uploadFile(any(), any()))
        .thenReturn(new FileUploadResult(givenServerUri, givenDownloadUrl));

    // when
    Member member =
        target.updateMemberNickNameAndProfileImg(givenMemberId, givenNickname, givenProfileImage);

    // then
    // S3Service.deleteFile() 메서드 인자 검증
    ArgumentCaptor<String> serverUriCapture = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).deleteFile(serverUriCapture.capture());
    assertEquals(originServerUri, serverUriCapture.getValue());

    // S3Service.uploadFile() 메서드 인자 검증
    ArgumentCaptor<MultipartFile> multipartFileArgumentCaptor =
        ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> directoryPathCapture = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1))
        .uploadFile(multipartFileArgumentCaptor.capture(), directoryPathCapture.capture());
    assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());
    assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());

    // 회원정보 수정 결과 검증
    assertEquals(givenNickname, member.getNickName());
    assertEquals(givenServerUri, member.getProfileImageUrl());
    assertEquals(givenDownloadUrl, member.getProfileImageDownLoadUrl());
  }

  public Member setMember(long givenMemberId, String givenEmail) {
    Member givenMember =
        MemberBuilder.fullData()
            .loginType(LoginType.EMAIL)
            .nickName("tempNicKName")
            .email(givenEmail)
            .password("tempPassword")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    return givenMember;
  }

  public Member setMemberToken(Member givenMember, String givenRefreshTokenValue) {
    MemberToken givenToken = new MemberToken(givenRefreshTokenValue);
    givenMember.updateRefreshToken(givenToken);
    return givenMember;
  }

  public String setMemberSignupByEmailCache(Member givenMember, String inputSecretNumber) {
    MemberSignupByEmailCache givenCache =
        new MemberSignupByEmailCache(givenMember, inputSecretNumber);
    return JsonUtil.convertObjectToJson(givenCache);
  }

  public void check_cacheRepository_saveCache(
      MemberEmailSignupDto givenDto, String givenSecretNumber) {
    ArgumentCaptor<String> cacheKeyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> cacheValueCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Long> expirationTimeCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockCacheRepository, times(1))
        .saveCache(
            cacheKeyCaptor.capture(), cacheValueCaptor.capture(), expirationTimeCaptor.capture());

    String expectedCacheKey = CacheTemplate.makeMemberSignupByEmailKey(givenDto.getEmail());
    String realCacheKey = cacheKeyCaptor.getValue();
    assertEquals(expectedCacheKey, realCacheKey);

    MemberSignupByEmailCache resultCache =
        JsonUtil.convertJsonToObject(cacheValueCaptor.getValue(), MemberSignupByEmailCache.class);
    checkMemberSignupByEmailCache(givenDto, resultCache, givenSecretNumber);

    assertEquals(mockMemberSignupRequestExpirationTime, expirationTimeCaptor.getValue());
  }

  public void check_emailService_sendMail(String givenEmail, String givenSecretNumber) {
    ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> emailTitleCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> emailContentCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockEmailService, times(1))
        .sendMail(emailCaptor.capture(), emailTitleCaptor.capture(), emailContentCaptor.capture());

    assertEquals(givenEmail, emailCaptor.getValue());
    assertEquals("[ShopingMall] 인증메일", emailTitleCaptor.getValue());

    String expectedEmailContent =
        String.format(
            "%s/member/signup?email=%s&secretNumber=%s  (3분 이내에 클릭)",
            mockDomain, givenEmail, givenSecretNumber.toString());
    String realEmailContent = emailContentCaptor.getValue();
    assertEquals(expectedEmailContent, realEmailContent);
  }

  public void check_memberRepository_save(Member givenMember) {
    ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
    verify(mockMemberRepository, times(1)).save(memberCaptor.capture());
    checkMember(givenMember, memberCaptor.getValue());
  }

  public void check_cacheRepository_removeCache(String givenEmail) {
    ArgumentCaptor<String> cacheKeyCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockCacheRepository, times(1)).removeCache(cacheKeyCaptor.capture());
    String expectCacheKey = CacheTemplate.makeMemberSignupByEmailKey(givenEmail);
    String realCacheKey = cacheKeyCaptor.getValue();
    assertEquals(expectCacheKey, realCacheKey);
  }

  public void checkMemberSignupByEmailCache(
      MemberEmailSignupDto givenDto,
      MemberSignupByEmailCache targetCache,
      String givenSecretNumber) {
    Member resultMember = targetCache.getMember();
    assertEquals(LoginType.EMAIL, resultMember.getLoginType());
    assertEquals(givenDto.getNickName(), resultMember.getNickName());
    assertEquals(givenDto.getEmail(), resultMember.getEmail());
    assertTrue(
        PasswordEncoderUtil.checkPassword(givenDto.getPassword(), resultMember.getPassword()));
    assertEquals(MemberRoleType.ROLE_MEMBER, resultMember.getRole());
    assertFalse(resultMember.getIsBan());
    assertEquals(givenSecretNumber.toString(), targetCache.getSecretNumber());
  }

  public void checkMember(Member expectedMember, Member realMember) {
    assertEquals(expectedMember.getId(), realMember.getId());
    assertEquals(expectedMember.getLoginType(), realMember.getLoginType());
    assertEquals(expectedMember.getNickName(), realMember.getNickName());
    assertEquals(expectedMember.getEmail(), realMember.getEmail());
    assertEquals(expectedMember.getPassword(), realMember.getPassword());
    assertEquals(expectedMember.getRole(), realMember.getRole());
    assertEquals(expectedMember.getIsBan(), realMember.getIsBan());
  }

  public void checkMemberToken(MemberToken expectedMemberToken, MemberToken realMemberToken) {
    assertEquals(expectedMemberToken.getId(), realMemberToken.getId());
    assertEquals(expectedMemberToken.getRefresh(), realMemberToken.getRefresh());
  }
}
