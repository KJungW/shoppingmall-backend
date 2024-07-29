package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.cache.EmailRegistrationCache;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.EmailRegistrationCacheError;
import com.project.shoppingmall.repository.CacheRepository;
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

  private final String cacheName = "mailRegister$";
  private final CacheRepository cacheRepository;
  private final RandomNumberGenerator randomNumberGenerator;
  private final EmailService emailService;
  private final MemberService memberService;
  private final JsonUtil jsonUtil;

  @Transactional
  public void sendCertificationEmail(Long memberId, String email) {
    String certificationNumber = randomNumberGenerator.makeRandomNumber();
    String title = "[ShopingMall] 인증메일";
    String content =
        String.format(
            "%s/email/registration?memberId=%s&certificationNumber=%s&email=%s  (3분 이내에 클릭)",
            domain, memberId.toString(), certificationNumber, email);

    String cacheJson =
        jsonUtil.convertObjectToJson(new EmailRegistrationCache(memberId, certificationNumber));
    cacheRepository.saveCache(cacheName + email, cacheJson, expirationTime);
    emailService.sendMail(email, title, content);
  }

  @Transactional
  public void registerEmail(Long memberId, String certificationNumber, String email) {
    try {
      String cacheJson = cacheRepository.getCache(cacheName + email).get();
      cacheRepository.removeCache(cacheName + email);
      EmailRegistrationCache cache =
          jsonUtil.convertJsonToObject(cacheJson, EmailRegistrationCache.class);
      validateEmailCache(cache, new EmailRegistrationCache(memberId, certificationNumber));
      Member member = memberService.findById(memberId).get();
      member.registerEmail(email);
    } catch (Exception ex) {
      throw new EmailRegistrationCacheError("캐시가 존재하지 않거나 유효하지 않습니다.");
    }
  }

  private void validateEmailCache(EmailRegistrationCache expected, EmailRegistrationCache target) {
    if (!expected.checkIsEquals(target)) {
      throw new EmailRegistrationCacheError("캐시가 유효하지 않습니다.");
    }
  }
}
