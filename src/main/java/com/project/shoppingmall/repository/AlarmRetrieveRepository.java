package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Alarm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRetrieveRepository extends JpaRepository<Alarm, Long> {

  @Query(
      "select a from Alarm a "
          + "left join fetch a.listener l "
          + "left join fetch a.targetReview rv "
          + "left join fetch a.targetProduct p "
          + "left join fetch a.targetRefund rf "
          + "where l.id = :listenerId")
  Slice<Alarm> retrieveAllByMember(@Param("listenerId") long listenerId, Pageable pageable);
}
