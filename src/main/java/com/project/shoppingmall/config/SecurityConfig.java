package com.project.shoppingmall.config;

import com.project.shoppingmall.handler.oauth2.OAuth2FailureHandler;
import com.project.shoppingmall.handler.oauth2.OAuth2SuccessHandler;
import com.project.shoppingmall.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CorsConfigurationSource corsConfigurationSource;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SuccessHandler oauth2SuccessHandler;
  private final OAuth2FailureHandler oauth2FailureHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable);

    http.formLogin(AbstractHttpConfigurer::disable);

    http.httpBasic(AbstractHttpConfigurer::disable);

    http.oauth2Login(
        (oauth2) ->
            oauth2
                .userInfoEndpoint(
                    (userInfoEndpointConfig) ->
                        userInfoEndpointConfig.userService(customOAuth2UserService))
                .failureHandler(oauth2FailureHandler)
                .successHandler(oauth2SuccessHandler));

    http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

    http.sessionManagement(
        (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource));

    return http.build();
  }
}
