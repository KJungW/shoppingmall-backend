package com.project.shoppingmall.service;

import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceTest {
  public EmailService emailService;
  public JavaMailSender mockMailSender;
  public MimeMessage mockMimeMessage;

  @BeforeEach
  public void beforeEach() {
    mockMimeMessage = mock(MimeMessage.class);
    mockMailSender = mock(JavaMailSender.class);
    emailService = new EmailService(mockMailSender);
  }

  @Test
  @DisplayName("EmailService.sendMail() : 정상흐름")
  public void sendMail_ok() {
    // given
    String givenEmail = "test@test.com";
    String givenTitle = "testTitle";
    String givenContent = "testContent";
    when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

    // when
    emailService.sendMail(givenEmail, givenTitle, givenContent);

    // then
    verify(mockMailSender, times(1)).createMimeMessage();
    verify(mockMailSender, times(1)).send(mockMimeMessage);
  }
}
