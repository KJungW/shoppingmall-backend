package com.project.shoppingmall.service.email;

import com.project.shoppingmall.dto.cache.EmailRegistrationCache;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.DuplicateMemberEmail;
import com.project.shoppingmall.exception.EmailRegistrationCacheError;
import com.project.shoppingmall.final_value.CacheTemplate;
import com.project.shoppingmall.repository.CacheRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.RandomNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailRegistrationService {
  @Value("${cache.expiration_time.mail_certificationNumber}")
  private Long expirationTime;

  @Value("${server.domain}")
  private String domain;

  private final CacheRepository cacheRepository;
  private final RandomNumberGenerator randomNumberGenerator;
  private final EmailService emailService;
  private final MemberService memberService;

  @Transactional
  public void sendCertificationEmail(Long memberId, String email) {
    if (memberService.findByEmail(email).isPresent())
      throw new DuplicateMemberEmail("중복된 이메일로 회원가입이 불가능합니다.");

    String certificationNumber = randomNumberGenerator.makeRandomNumber();
    String title = "[ShopingMall] 인증메일";
    String content =
        String.format(
            "%s/email/registration?memberId=%s&certificationNumber=%s&email=%s  (3분 이내에 클릭)",
            domain, memberId.toString(), certificationNumber, email);

    String cacheKey = CacheTemplate.makeEmailRegisterCacheKey(memberId);
    String cacheValue =
        JsonUtil.convertObjectToJson(new EmailRegistrationCache(memberId, certificationNumber));
    cacheRepository.saveCache(cacheKey, cacheValue, expirationTime);
    emailService.sendMail(email, title, content);
  }

  @Transactional
  public void registerEmail(Long memberId, String certificationNumber, String email) {
    String cacheKey = CacheTemplate.makeEmailRegisterCacheKey(memberId);
    String cacheValueJson =
        cacheRepository
            .getCache(cacheKey)
            .orElseThrow(() -> new EmailRegistrationCacheError("캐시가 존재하지 않거나 유효하지 않습니다."));
    Member member =
        memberService.findById(memberId).orElseThrow(() -> new DataNotFound("존재하지 않는 회원입니다."));

    if (memberService.findByEmail(email).isPresent())
      throw new DuplicateMemberEmail("중복된 이메일로 회원가입이 불가능합니다.");

    EmailRegistrationCache cache =
        JsonUtil.convertJsonToObject(cacheValueJson, EmailRegistrationCache.class);
    validateEmailCache(cache, new EmailRegistrationCache(memberId, certificationNumber));
    member.registerEmail(email);

    cacheRepository.removeCache(cacheKey);
  }

  private void validateEmailCache(EmailRegistrationCache expected, EmailRegistrationCache target) {
    if (!expected.checkIsEquals(target)) {
      throw new EmailRegistrationCacheError("캐시가 유효하지 않습니다.");
    }
  }
}
