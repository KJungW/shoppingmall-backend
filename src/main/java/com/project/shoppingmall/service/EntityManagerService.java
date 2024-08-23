package com.project.shoppingmall.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EntityManagerService {
  private final EntityManager em;

  public void flush() {
    em.flush();
  }
}
