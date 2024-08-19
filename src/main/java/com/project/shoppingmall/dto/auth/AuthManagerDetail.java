package com.project.shoppingmall.dto.auth;

import com.project.shoppingmall.type.ManagerRoleType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AuthManagerDetail implements UserDetails {
  private final Long id;
  private final ManagerRoleType role;

  public AuthManagerDetail(Long id, ManagerRoleType role) {
    this.id = id;
    this.role = role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<String> roles = new ArrayList<>();
    roles.add(role.toString());
    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return id.toString();
  }
}
