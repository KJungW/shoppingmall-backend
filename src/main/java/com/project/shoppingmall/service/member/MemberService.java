package com.project.shoppingmall.service.member;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.type.LoginType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
  private final MemberRepository memberRepository;
  private final S3Service s3Service;

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
