package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Manager;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

  @Query("select m from Manager m " + "where m.role = 'ROLE_ROOT_MANAGER' ")
  Optional<Manager> findRootManger();

  @EntityGraph(attributePaths = {"token"})
  Optional<Manager> findBySerialNumber(String serialNumber);
}
