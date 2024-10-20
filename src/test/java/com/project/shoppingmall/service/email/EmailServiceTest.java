package com.project.shoppingmall.service.email;

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
    String inputEmail = "test@test.com";
    String inputTitle = "testTitle";
    String inputContent = "testContent";
    when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

    // when
    emailService.sendMail(inputEmail, inputTitle, inputContent);

    // then
    verify(mockMailSender, times(1)).createMimeMessage();
    verify(mockMailSender, times(1)).send(mockMimeMessage);
  }
}
