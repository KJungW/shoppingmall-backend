package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.type.LoginType;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

class MemberServiceTest {
  private MemberService memberService;
  private MemberRepository memberRepository;
  private S3Service s3Service;

  @BeforeEach
  public void beforeEach() {
    memberRepository = Mockito.mock(MemberRepository.class);
    s3Service = Mockito.mock(S3Service.class);
    memberService = new MemberService(memberRepository, s3Service);
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

    Mockito.when(memberRepository.findById(Mockito.any())).thenReturn(Optional.of(givenMember));
    Mockito.when(s3Service.uploadFile(Mockito.any(), Mockito.any()))
        .thenReturn(new FileUploadResult(givenServerUri, givenDownloadUrl));

    // when
    Member member =
        memberService.updateMemberNickNameAndProfileImg(
            givenMemberId, givenNickname, givenProfileImage);

    // then
    // 기존에 저장된 프로필이미지 데이터가 없으므로, S3Service.deleteFile()가 실행되지 않아야한다.
    Mockito.verify(s3Service, Mockito.times(0)).deleteFile(Mockito.any());

    // S3Service.uploadFile() 메서드 인자 검증
    ArgumentCaptor<MultipartFile> multipartFileArgumentCaptor =
        ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> directoryPathCapture = ArgumentCaptor.forClass(String.class);
    Mockito.verify(s3Service, Mockito.times(1))
        .uploadFile(multipartFileArgumentCaptor.capture(), directoryPathCapture.capture());
    Assertions.assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());
    Assertions.assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());

    // 회원정보 수정 결과 검증
    Assertions.assertEquals(givenNickname, member.getNickName());
    Assertions.assertEquals(givenServerUri, member.getProfileImageUrl());
    Assertions.assertEquals(givenDownloadUrl, member.getProfileImageDownLoadUrl());
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

    Mockito.when(memberRepository.findById(Mockito.any())).thenReturn(Optional.of(givenMember));
    Mockito.when(s3Service.uploadFile(Mockito.any(), Mockito.any()))
        .thenReturn(new FileUploadResult(givenServerUri, givenDownloadUrl));

    // when
    Member member =
        memberService.updateMemberNickNameAndProfileImg(
            givenMemberId, givenNickname, givenProfileImage);

    // then
    // S3Service.deleteFile() 메서드 인자 검증
    ArgumentCaptor<String> serverUriCapture = ArgumentCaptor.forClass(String.class);
    Mockito.verify(s3Service, Mockito.times(1)).deleteFile(serverUriCapture.capture());
    Assertions.assertEquals(originServerUri, serverUriCapture.getValue());

    // S3Service.uploadFile() 메서드 인자 검증
    ArgumentCaptor<MultipartFile> multipartFileArgumentCaptor =
        ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<String> directoryPathCapture = ArgumentCaptor.forClass(String.class);
    Mockito.verify(s3Service, Mockito.times(1))
        .uploadFile(multipartFileArgumentCaptor.capture(), directoryPathCapture.capture());
    Assertions.assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());
    Assertions.assertEquals(givenProfileImage, multipartFileArgumentCaptor.getValue());

    // 회원정보 수정 결과 검증
    Assertions.assertEquals(givenNickname, member.getNickName());
    Assertions.assertEquals(givenServerUri, member.getProfileImageUrl());
    Assertions.assertEquals(givenDownloadUrl, member.getProfileImageDownLoadUrl());
  }
}
