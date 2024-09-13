package com.project.shoppingmall.controller_manage.root_manager;

import com.project.shoppingmall.controller_manage.root_manager.input.InputOnOffManagerMode;
import com.project.shoppingmall.controller_manage.root_manager.output.OutputMakeCommonManger;
import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.dto.manager.MakeManagerResult;
import com.project.shoppingmall.service_manage.common.RootManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RootMangerController {
  private final RootManagerService rootManagerService;

  @PostMapping("root-manager/manager")
  @PreAuthorize("hasRole('ROLE_ROOT_MANAGER')")
  public OutputMakeCommonManger makeCommonManager() {
    AuthManagerDetail userDetail =
        (AuthManagerDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    MakeManagerResult makeManagerResult = rootManagerService.makeManager(userDetail.getId());
    return new OutputMakeCommonManger(makeManagerResult);
  }

  @PostMapping("root-manager/manager-mode")
  @PreAuthorize("hasRole('ROLE_ROOT_MANAGER')")
  public void onOffManagerMode(@Valid @RequestBody InputOnOffManagerMode input) {
    AuthManagerDetail userDetail =
        (AuthManagerDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    rootManagerService.onOffManagerMode(userDetail.getId(), input.getIsOn());
  }
}
