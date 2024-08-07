package com.project.shoppingmall.config;

import com.siot.IamportRestClient.IamportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortOneConfig {
  @Value("${port_one.api_key}")
  String apiKey;

  @Value("${port_one.secret_key}")
  String secretKey;

  @Bean
  public IamportClient iamportClient() {
    return new IamportClient(apiKey, secretKey);
  }
}
