package com.project.shoppingmall.config;

import com.project.shoppingmall.filter.JwtFilter;
import com.project.shoppingmall.filter.ManagerModeFilter;
import com.project.shoppingmall.handler.auth.AuthorizationFailureHandler;
import com.project.shoppingmall.handler.oauth2.OAuth2FailureHandler;
import com.project.shoppingmall.handler.oauth2.OAuth2SuccessHandler;
import com.project.shoppingmall.service.auth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CorsConfigurationSource corsConfigurationSource;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SuccessHandler oauth2SuccessHandler;
  private final OAuth2FailureHandler oauth2FailureHandler;
  private final AuthenticationEntryPoint authenticationFailureHandler;
  private final AuthorizationFailureHandler authorizationFailureHandler;
  private final JwtFilter jwtFilter;
  private final ManagerModeFilter managerModeFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable);

    http.formLogin(AbstractHttpConfigurer::disable);

    http.httpBasic(AbstractHttpConfigurer::disable);

    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(managerModeFilter, JwtFilter.class);

    http.oauth2Login(
        (oauth2) ->
            oauth2
                .userInfoEndpoint(
                    (userInfoEndpointConfig) ->
                        userInfoEndpointConfig.userService(customOAuth2UserService))
                .failureHandler(oauth2FailureHandler)
                .successHandler(oauth2SuccessHandler));

    http.exceptionHandling(
        (exceptionHandling) ->
            exceptionHandling
                .authenticationEntryPoint(authenticationFailureHandler)
                .accessDeniedHandler(authorizationFailureHandler));

    http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

    http.sessionManagement(
        (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource));

    return http.build();
  }
}
