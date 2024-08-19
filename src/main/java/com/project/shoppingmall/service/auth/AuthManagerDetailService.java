package com.project.shoppingmall.service.auth;

import com.project.shoppingmall.dto.auth.AuthManagerDetail;
import com.project.shoppingmall.entity.Manager;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.manager.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthManagerDetailService implements UserDetailsService {
  private final ManagerService managerService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    long id = Long.parseLong(username);
    Manager manager =
        managerService
            .findById(id)
            .orElseThrow(() -> new DataNotFound("현재 ID에 해당하는 관리자가 존재하지 않습니다."));
    return new AuthManagerDetail(manager.getId(), manager.getRole());
  }
}
