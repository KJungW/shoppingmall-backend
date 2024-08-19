package com.project.shoppingmall.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.manager.ManagerService;
import com.project.shoppingmall.testdata.ManagerBuilder;
import com.project.shoppingmall.type.ManagerRoleType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
    // - username 인자 세팅
    Long givenMemberId = 10L;
    String givenMemberIdString = String.valueOf(givenMemberId);

    // - managerService.findById() 세팅
    ManagerRoleType givenRoleType = ManagerRoleType.ROLE_COMMON_MANAGER;
    Manager givenManager = ManagerBuilder.fullData().role(givenRoleType).build();
    ReflectionTestUtils.setField(givenManager, "id", givenMemberId);
    when(mockManagerService.findById(anyLong())).thenReturn(Optional.of(givenManager));

    // when
    AuthManagerDetail resultUserDetail =
        (AuthManagerDetail) target.loadUserByUsername(givenMemberIdString);

    // then
    assertEquals(givenMemberId, resultUserDetail.getId());
    assertEquals(givenRoleType, resultUserDetail.getRole());
    assertEquals(givenMemberIdString, resultUserDetail.getUsername());
    assertNull(resultUserDetail.getPassword());
    assertEquals(1, resultUserDetail.getAuthorities().size());
    assertEquals(
        ManagerRoleType.ROLE_COMMON_MANAGER.toString(),
        resultUserDetail.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  @DisplayName("loadUserByUsername() : 조회된 회원이 없음")
  public void loadUserByUsername_NoMember() {
    // given
    // - username 인자 세팅
    Long givenMemberId = 10L;
    String givenMemberIdString = String.valueOf(givenMemberId);

    // - memberService.findById() 세팅
    when(mockManagerService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.loadUserByUsername(givenMemberIdString));
  }
}
