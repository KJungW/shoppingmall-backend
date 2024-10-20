package com.project.shoppingmall.config;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.addAllowedOriginPattern("*");
    configuration.setAllowedMethods(Collections.singletonList("*"));
    configuration.setAllowedHeaders(Collections.singletonList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
    configuration.setExposedHeaders(Collections.singletonList("refresh"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
