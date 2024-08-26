package com.project.shoppingmall.controller.alarm;

import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AlarmController {
  private final AlarmDeleteService alarmDeleteService;

  @DeleteMapping("alarm/{alarmId}")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void deleteAlarm(@PathVariable("alarmId") Long alarmId) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    alarmDeleteService.deleteAlarmByListener(userDetail.getId(), alarmId);
  }
}
