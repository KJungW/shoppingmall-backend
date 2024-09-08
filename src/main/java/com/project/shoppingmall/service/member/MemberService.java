package com.project.shoppingmall.service.member;

import com.project.shoppingmall.dto.cache.MemberSignupByEmailCache;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.member.MemberEmailSignupDto;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.MemberToken;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.DuplicateMemberEmail;
import com.project.shoppingmall.exception.MemberSignupByEmailCacheError;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.service.email.EmailService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.JwtUtil;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
  private final MemberRepository memberRepository;
  private final CacheRepository cacheRepository;
  private final S3Service s3Service;
  private final EmailService emailService;
  private final JwtUtil jwtUtil;

  @Value("${cache.expiration_time.member_signup_request}")
  private Long memberSignupRequestExpirationTime;

  @Value("${server.domain}")
  private String domain;

  @Transactional
  public Member save(Member member) {
    return memberRepository.save(member);
  }

  @Transactional
  public void requestSignupByEmail(MemberEmailSignupDto dto) {
    if (findByEmail(dto.getEmail()).isPresent())
      throw new DuplicateMemberEmail("중복된 이메일로 회원가입이 불가능합니다.");

    Member member =
        Member.builder()
            .loginType(LoginType.EMAIL)
            .nickName(dto.getNickName())
            .email(dto.getEmail())
            .password(dto.getPassword())
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();

    String secretNumber = UUID.randomUUID().toString();
    String cacheKey = CacheTemplate.makeMemberSignupByEmailKey(dto.getEmail());
    String cacheValue =
        JsonUtil.convertObjectToJson(new MemberSignupByEmailCache(member, secretNumber));
    cacheRepository.saveCache(cacheKey, cacheValue, memberSignupRequestExpirationTime);

    String emailTitle = "[ShopingMall] 인증메일";
    String emailContent =
        String.format(
            "%s/member/signup?email=%s&secretNumber=%s  (3분 이내에 클릭)",
            domain, dto.getEmail(), secretNumber);
    emailService.sendMail(dto.getEmail(), emailTitle, emailContent);
  }

  @Transactional
  public Member signupByEmail(String email, String secretNumber) {
    String cacheKey = CacheTemplate.makeMemberSignupByEmailKey(email);
    String signupCacheJson =
        cacheRepository
            .getCache(cacheKey)
            .orElseThrow(
                () -> new MemberSignupByEmailCacheError("시크릿키에 해당하는 캐시가 존재하지 않거나 유효시간이 지났습니다."));

    MemberSignupByEmailCache signupCache =
        JsonUtil.convertJsonToObject(signupCacheJson, MemberSignupByEmailCache.class);
    if (!signupCache.getSecretNumber().equals(secretNumber))
      throw new MemberSignupByEmailCacheError("시크릿키가 올바르지 않습니다.");

    Member targetMember = signupCache.getMember();
    if (findByEmail(targetMember.getEmail()).isPresent())
      throw new DuplicateMemberEmail("중복된 이메일로 회원가입이 불가능합니다.");

    String refreshToken =
        jwtUtil.createRefreshToken(
            new RefreshTokenData(targetMember.getId(), targetMember.getRole().toString()));
    MemberToken memberToken = new MemberToken(refreshToken);
    targetMember.updateRefreshToken(memberToken);
    save(targetMember);

    cacheRepository.removeCache(cacheKey);
    return targetMember;
  }

  public Optional<Member> findById(Long memberId) {
    return memberRepository.findById(memberId);
  }

  public Optional<Member> findByLonginTypeAndSocialId(LoginType loginType, String socialID) {
    return memberRepository.findByLoginTypeAndSocialId(loginType, socialID);
  }

  public Optional<Member> findByEmail(String email) {
    return memberRepository.findByEmail(email);
  }

  @Transactional
  public Member updateMemberNickNameAndProfileImg(
      Long memberId, String nickName, MultipartFile profileImg) {
    Member member =
        findById(memberId).orElseThrow(() -> new DataNotFound("id에 해당하는 데이터가 존재하지 않습니다."));
    if (!Strings.isEmpty(member.getProfileImageDownLoadUrl())) {
      s3Service.deleteFile(member.getProfileImageUrl());
    }
    FileUploadResult uploadResult =
        s3Service.uploadFile(profileImg, "profileImg/" + member.getId() + "-" + nickName + "/");
    member.updateProfile(uploadResult.getFileServerUri(), uploadResult.getDownLoadUrl());
    member.updateNickName(nickName);
    return member;
  }
}
