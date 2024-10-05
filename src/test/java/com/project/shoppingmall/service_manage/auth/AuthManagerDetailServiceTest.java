package com.project.shoppingmall.service_manage.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service_manage.common.ManagerService;
import com.project.shoppingmall.test_dto.auth.AuthManagerDetailManager;
import com.project.shoppingmall.test_entity.manager.ManagerBuilder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthManagerDetailServiceTest {
  private AuthManagerDetailService target;
  private ManagerService mockManagerService;

  @BeforeEach
  public void beforeEach() {
    mockManagerService = mock(ManagerService.class);
    target = new AuthManagerDetailService(mockManagerService);
  }

  @Test
  @DisplayName("loadUserByUsername() : 정상흐름")
  public void loadUserByUsername_ok() {
    // given
    String inputUserName = String.valueOf(10L);

    Manager givenManager = ManagerBuilder.make(Integer.parseInt(inputUserName));

    when(mockManagerService.findById(anyLong())).thenReturn(Optional.of(givenManager));

    // when
    AuthManagerDetail result = (AuthManagerDetail) target.loadUserByUsername(inputUserName);

    // then
    AuthManagerDetailManager.check(givenManager, result);
  }

  @Test
  @DisplayName("loadUserByUsername() : 조회된 회원이 없음")
  public void loadUserByUsername_NoMember() {
    // given
    String inputUserName = String.valueOf(10L);

    // - memberService.findById() 세팅
    when(mockManagerService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.loadUserByUsername(inputUserName));
  }
}
