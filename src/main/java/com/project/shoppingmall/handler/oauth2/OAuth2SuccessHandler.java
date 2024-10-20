package com.project.shoppingmall.handler.oauth2;

import com.project.shoppingmall.dto.oauth2.OAuth2UserPrinciple;
import com.project.shoppingmall.dto.oauth2.user_info.OAuth2UserInfo;
import com.project.shoppingmall.dto.token.RefreshTokenData;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.MemberToken;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.CookieUtil;
import com.project.shoppingmall.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  private final MemberFindService memberFindService;
  private final MemberService memberService;
  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;

  @Value("${frontend.login_success_url}")
  private String loginSuccessRedirectionUrl;

  public OAuth2SuccessHandler(
      MemberFindService memberFindService,
      MemberService memberService,
      JwtUtil jwtUtil,
      CookieUtil cookieUtil) {
    this.memberFindService = memberFindService;
    this.memberService = memberService;
    this.jwtUtil = jwtUtil;
    this.cookieUtil = cookieUtil;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    OAuth2UserPrinciple userPrinciple = (OAuth2UserPrinciple) authentication.getPrincipal();
    OAuth2UserInfo userInfo = userPrinciple.getUserInfo();

    Optional<Member> queryResult = findMemberByOAuth2UserInfo(userInfo);
    Member member =
        queryResult
            .map(savedMember -> updateMember(savedMember, userInfo))
            .orElseGet(() -> createMember(userInfo));

    MemberToken memberToken = makeRefreshToken(member);
    member.updateRefreshToken(memberToken);

    ResponseCookie cookie =
        cookieUtil.createCookie(
            "refresh",
            memberToken.getRefresh(),
            (int) (jwtUtil.getRefreshExpirationTimeMs() / 1000));

    response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    response.sendRedirect(loginSuccessRedirectionUrl);
  }

  private Optional<Member> findMemberByOAuth2UserInfo(OAuth2UserInfo oAuth2UserInfo) {
    return memberFindService.findByLonginTypeAndSocialId(
        oAuth2UserInfo.getLoginType(), oAuth2UserInfo.getSocialId());
  }

  private Member createMember(OAuth2UserInfo userInfo) {
    Member newMember =
        Member.builder()
            .loginType(userInfo.getLoginType())
            .socialId(userInfo.getSocialId())
            .nickName(userInfo.getName())
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    memberService.save(newMember);
    return newMember;
  }

  private Member updateMember(Member member, OAuth2UserInfo userInfo) {
    member.updateNickName(userInfo.getName());
    return member;
  }

  private MemberToken makeRefreshToken(Member member) {
    String refreshToken =
        jwtUtil.createRefreshToken(
            new RefreshTokenData(member.getId(), member.getRole().toString()));
    MemberToken memberToken = new MemberToken(refreshToken);
    member.updateRefreshToken(memberToken);
    return memberToken;
  }
}
