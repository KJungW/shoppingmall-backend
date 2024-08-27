package com.project.shoppingmall.controller.alarm_retrive;

import com.project.shoppingmall.controller.alarm_retrive.output.OutputRetrieveAllByMember;
import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.alarm.AlarmDto;
import com.project.shoppingmall.dto.auth.AuthMemberDetail;
import com.project.shoppingmall.service.alarm.AlarmRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AlarmRetrieveController {
  private final AlarmRetrieveService alarmRetrieveService;

  @GetMapping("member/alarms")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputRetrieveAllByMember retrieveAllByMember(
      @RequestParam("sliceNumber") Integer sliceNumber,
      @RequestParam("sliceSize") Integer sliceSize) {
    AuthMemberDetail userDetail =
        (AuthMemberDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SliceResult<AlarmDto> sliceResult =
        alarmRetrieveService.retrieveAllByMember(sliceNumber, sliceSize, userDetail.getId());
    return new OutputRetrieveAllByMember(sliceResult);
  }
}
