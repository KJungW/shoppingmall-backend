package com.project.shoppingmall.config;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
  @Value("${spring.cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${spring.cloud.aws.credentials.secret-key}")
  private String accessSecret;

  @Value("${spring.cloud.aws.region.static}")
  private String region;

  @Value("${spring.cloud.aws.endpoint}")
  private String awsEndPoint;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .forcePathStyle(true)
        .credentialsProvider(awsCredentialsProvider())
        .region(Region.of(region))
        .endpointOverride(URI.create(awsEndPoint))
        .build();
  }

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    return AwsCredentialsProviderChain.builder()
        .reuseLastProviderEnabled(true)
        .credentialsProviders(
            List.of(
                DefaultCredentialsProvider.create(),
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, accessSecret))))
        .build();
  }
}
