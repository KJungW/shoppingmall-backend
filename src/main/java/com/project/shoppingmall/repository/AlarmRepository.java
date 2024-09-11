package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Alarm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

  @Query("select a from Alarm a " + "left join a.targetProduct p " + "where p.id = :productId ")
  List<Alarm> findAlarmByTargetProduct(@Param("productId") long productId);

  @Query("select a from Alarm a " + "left join a.targetReview r " + "where r.id = :reviewId ")
  List<Alarm> findAlarmByTargetReview(@Param("reviewId") long reviewId);

  @Query("select a from Alarm a " + "left join a.listener l " + "where l.id = :listenerId ")
  List<Alarm> findAllByListener(@Param("listenerId") long listenerId);
}
