package com.project.shoppingmall.service;

import com.project.shoppingmall.exception.FailSendEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  public void sendMail(String email, String title, String content) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
      helper.setTo(email);
      helper.setSubject(title);
      helper.setText(content);
      mailSender.send(mimeMessage);
    } catch (MessagingException ex) {
      throw new FailSendEmail("이메일 전송에 실패했습니다.");
    }
  }
}
