package com.project.shoppingmall.service.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.cache.MemberSignupByEmailCache;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.member.MemberEmailSignupDto;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.MemberToken;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.DuplicateMemberEmail;
import com.project.shoppingmall.exception.MemberAccountIsNotRegistered;
import com.project.shoppingmall.exception.MemberSignupByEmailCacheError;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.service.email.EmailService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
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
  private MemberFindService mockMemberFindService;
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
    mockMemberFindService = mock(MemberFindService.class);
    mockCacheRepository = mock(CacheRepository.class);
    mockS3Service = mock(S3Service.class);
    mockEmailService = mock(EmailService.class);
    mockJwtUtil = mock(JwtUtil.class);

    target =
        new MemberService(
            mockMemberRepository,
            mockMemberFindService,
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

    set_memberFindService_findByEmail();
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

    set_memberFindService_findByEmail(duplicateEmailMember);
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
    Member givenMember = MemberBuilder.makeMember(givenMemberId, LoginType.EMAIL, inputEmail);
    String givenCacheJson = setMemberSignupByEmailCache(givenMember, inputSecretNumber);
    setMemberToken(givenMember, givenRefreshTokenValue);

    set_cacheRepository_getCache(givenCacheJson);
    set_memberFindService_findByEmail();
    set_jwtUtil_createRefreshToken(givenRefreshTokenValue);

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

    set_cacheRepository_getCache();

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
    Member givenMember = MemberBuilder.makeMember(givenMemberId, LoginType.EMAIL, inputEmail);
    String givenCacheJson = setMemberSignupByEmailCache(givenMember, wrongSecretKey);
    setMemberToken(givenMember, givenRefreshTokenValue);

    set_cacheRepository_getCache(givenCacheJson);
    set_memberFindService_findByEmail();
    set_jwtUtil_createRefreshToken(givenRefreshTokenValue);

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
    Member givenMember = MemberBuilder.makeMember(givenMemberId, LoginType.EMAIL, inputEmail);
    Member otherMember = MemberBuilder.fullData().build();
    String givenCacheJson = setMemberSignupByEmailCache(givenMember, inputSecretNumber);
    setMemberToken(givenMember, givenRefreshTokenValue);

    set_cacheRepository_getCache(givenCacheJson);
    set_memberFindService_findByEmail(otherMember);

    // when then
    assertThrows(
        DuplicateMemberEmail.class, () -> target.signupByEmail(inputEmail, inputSecretNumber));
  }

  @Test
  @DisplayName("updateMemberNickNameAndProfileImg() : 정상 흐름")
  public void updateMemberNickNameAndProfileImg_ok() throws IOException {
    // given
    Long inputMemberId = 10L;
    String inputNickName = "testNickName";
    MultipartFile inputProfileImage =
        setProfileImage("profileSampleImage.png", "static/profileSampleImage.png");

    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    String givenServerUri = "testServerUri";
    String givenDownloadUrl = "testDownLoadUrl";

    set_mockMemberFindService_findById(givenMember);
    set_s3Service_uploadFile(givenServerUri, givenDownloadUrl);

    // when
    Member resultMember =
        target.updateMemberNickNameAndProfileImg(inputMemberId, inputNickName, inputProfileImage);

    // then
    checkMember(givenMember, resultMember);
    check_s3Service_deleteFile_notRunning();
    check_s3Service_uploadFile(givenMember, inputProfileImage);
  }

  @Test
  @DisplayName("updateMemberNickNameAndProfileImg() : 기존 데이터 덮어씌우기")
  public void updateMemberNickNameAndProfileImg_dataOverwrite() throws IOException {
    // given
    Long inputMemberId = 10L;
    String inputNickName = "testNickName";
    MultipartFile inputProfileImage =
        setProfileImage("profileSampleImage.png", "static/profileSampleImage.png");

    String originServerUri = "originServerUri";
    String originDownloadUrl = "originDownLoadUrl";
    String newServerUri = "testServerUri";
    String newDownloadUrl = "testDownLoadUrl";
    Member givenMember =
        MemberBuilder.makeMemberWithProfileImage(
            inputMemberId, LoginType.NAVER, originServerUri, originDownloadUrl);

    set_mockMemberFindService_findById(givenMember);
    set_s3Service_uploadFile(newServerUri, newDownloadUrl);

    // when
    Member resultMember =
        target.updateMemberNickNameAndProfileImg(inputMemberId, inputNickName, inputProfileImage);

    // then
    check_s3Service_deleteFile(originServerUri);
    check_s3Service_uploadFile(givenMember, inputProfileImage);
    checkMember(givenMember, resultMember);
  }

  @Test
  @DisplayName("loginByEmail() : 정상흐름")
  public void loginByEmail_ok() {
    // given
    String inputEmail = "test@test.com";
    String inputPassword = "gdfg123!@#";

    Member givenMember = MemberBuilder.makeMember(10L, LoginType.EMAIL, inputEmail, inputPassword);
    String givenRefreshTokenValue = "givenRefreshTokenValue";
    MemberToken givenMemberToken = setMemberToken(givenMember, givenRefreshTokenValue).getToken();

    set_memberFindService_findByEmail(givenMember);
    set_jwtUtil_createRefreshToken(givenRefreshTokenValue);

    // when
    Member resultMember = target.loginByEmail(inputEmail, inputPassword);

    // then
    checkMember(givenMember, resultMember);
    checkMemberToken(givenMemberToken, resultMember.getToken());
  }

  @Test
  @DisplayName("loginByEmail() : 이메일에 해당하는 회원이 존재하지 않음")
  public void loginByEmail_notMember() {
    // given
    String inputEmail = "test@test.com";
    String inputPassword = "gdfg123!@#";

    set_memberFindService_findByEmail();

    // when then
    assertThrows(DataNotFound.class, () -> target.loginByEmail(inputEmail, inputPassword));
  }

  @Test
  @DisplayName("loginByEmail() : 비밀번호가 맞지 않을 경우")
  public void loginByEmail_incorrectPassword() {
    // given
    String inputEmail = "test@test.com";
    String inputPassword = "gdfg123!@#";

    String otherPassword = "dsafkjwelr";
    Member givenMember = MemberBuilder.makeMember(10L, LoginType.EMAIL, inputEmail, otherPassword);

    set_memberFindService_findByEmail(givenMember);

    // when then
    assertThrows(DataNotFound.class, () -> target.loginByEmail(inputEmail, inputPassword));
  }

  @Test
  @DisplayName("registerAccount() : 정상흐름")
  public void registerAccount_ok() {
    // given
    long inputMemberId = 10L;
    String inputAccountNumber = "12321-5124-12312";

    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    set_mockMemberFindService_findById(givenMember);

    // when
    target.registerAccount(inputMemberId, inputAccountNumber);

    // then
    assertEquals(inputAccountNumber, givenMember.getAccountNumber());
  }

  @Test
  @DisplayName("getAccountNumber() : 정상흐름")
  public void getAccountNumber_ok() {
    // given
    long inputMemberId = 10L;

    Member givenMember =
        MemberBuilder.makeMemberWithAccountNumber(
            inputMemberId, LoginType.NAVER, "12321-5124-12312");
    set_mockMemberFindService_findById(givenMember);

    // when
    String resultAccountNumber = target.getAccountNumber(inputMemberId);

    // then
    assertEquals(givenMember.getAccountNumber(), resultAccountNumber);
  }

  @Test
  @DisplayName("getAccountNumber() : 계좌를 등록하지 않은 회원의 계좌 조회")
  public void getAccountNumber_notRegisteredAccount() {
    // given
    long inputMemberId = 10L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    set_mockMemberFindService_findById(givenMember);

    // when
    assertThrows(MemberAccountIsNotRegistered.class, () -> target.getAccountNumber(inputMemberId));
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

  public MultipartFile setProfileImage(String name, String path) throws IOException {
    return new MockMultipartFile(name, new FileInputStream(new ClassPathResource(path).getFile()));
  }

  public void checkMember(Member expectedMember, Member realMember) {
    assertEquals(expectedMember.getId(), realMember.getId());
    assertEquals(expectedMember.getLoginType(), realMember.getLoginType());
    assertEquals(expectedMember.getNickName(), realMember.getNickName());
    assertEquals(expectedMember.getEmail(), realMember.getEmail());
    assertEquals(expectedMember.getPassword(), realMember.getPassword());
    assertEquals(expectedMember.getRole(), realMember.getRole());
    assertEquals(expectedMember.getIsBan(), realMember.getIsBan());
    assertEquals(expectedMember.getProfileImageUrl(), realMember.getProfileImageUrl());
    assertEquals(
        expectedMember.getProfileImageDownLoadUrl(), realMember.getProfileImageDownLoadUrl());
  }

  public void checkMemberToken(MemberToken expectedMemberToken, MemberToken realMemberToken) {
    assertEquals(expectedMemberToken.getId(), realMemberToken.getId());
    assertEquals(expectedMemberToken.getRefresh(), realMemberToken.getRefresh());
  }

  public void set_memberFindService_findByEmail() {
    when(mockMemberFindService.findByEmail(anyString())).thenReturn(Optional.empty());
  }

  public void set_memberFindService_findByEmail(Member givenMember) {
    when(mockMemberFindService.findByEmail(anyString())).thenReturn(Optional.of(givenMember));
  }

  public void set_jwtUtil_createRefreshToken(String givenRefreshToken) {
    when(mockJwtUtil.createRefreshToken(any())).thenReturn(givenRefreshToken);
  }

  public void set_cacheRepository_getCache() {
    when(mockCacheRepository.getCache(anyString())).thenReturn(Optional.empty());
  }

  public void set_cacheRepository_getCache(String givenCacheJson) {
    when(mockCacheRepository.getCache(anyString())).thenReturn(Optional.of(givenCacheJson));
  }

  public void set_mockMemberFindService_findById(Member givenMember) {
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
  }

  public void set_s3Service_uploadFile(String givenServerUri, String givenDownloadUrl) {
    when(mockS3Service.uploadFile(any(), any()))
        .thenReturn(new FileUploadResult(givenServerUri, givenDownloadUrl));
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

  public void check_s3Service_deleteFile_notRunning() {
    verify(mockS3Service, times(0)).deleteFile(any());
  }

  public void check_s3Service_deleteFile(String givenServerUri) {
    ArgumentCaptor<String> serverUriCapture = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).deleteFile(serverUriCapture.capture());
    assertEquals(givenServerUri, serverUriCapture.getValue());
  }

  public void check_s3Service_uploadFile(Member givenMember, MultipartFile givenProfileImage) {
    ArgumentCaptor<MultipartFile> imageCaptor = ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> uriCapture = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).uploadFile(imageCaptor.capture(), uriCapture.capture());

    assertEquals(givenProfileImage, imageCaptor.getValue());

    String expectedUri =
        "profileImg/" + givenMember.getId() + "-" + givenMember.getNickName() + "/";
    String realUri = uriCapture.getValue();
    assertEquals(expectedUri, realUri);
  }
}
