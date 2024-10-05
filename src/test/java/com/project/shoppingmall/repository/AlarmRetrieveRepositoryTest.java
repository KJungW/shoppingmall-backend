package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import com.project.shoppingmall.type.RefundStateType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class AlarmRetrieveRepositoryTest {
  @Autowired private AlarmRetrieveRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;

  private long alarmListenerId;

  @BeforeEach
  public void beforeEach() {
    Member listener = testDataMaker.saveMember();
    Product listenerProduct = testDataMaker.saveProduct(listener);
    Review listenerReview = testDataMaker.saveReview(listener);
    Refund listenerTargerRefund =
        testDataMaker.saveRefund(listenerProduct, RefundStateType.COMPLETE);
    for (int i = 0; i < 3; i++) {
      testDataMaker.saveMemberBanAlarm(listener);
      testDataMaker.saveProductBanAlarm(listener, listenerProduct);
      testDataMaker.saveReviewBanAlarm(listener, listenerReview);
      testDataMaker.saveRefundRequestAlarm(listener, listenerTargerRefund);
    }
    em.flush();
    em.clear();

    alarmListenerId = listener.getId();
  }

  @Test
  @DisplayName("retrieveAllByMember() : 정상흐름")
  public void retrieveAllByMember_ok() {
    // given
    long inputListenerId = alarmListenerId;
    PageRequest inputPageRequest = PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<Alarm> result = target.retrieveAllByMember(inputListenerId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_retrieveAllByMember(result);
    checkWhere_retrieveAllByMember(alarmListenerId, result);
  }

  public void checkFetchLoad_retrieveAllByMember(Slice<Alarm> target) {
    PersistenceUnitUtil emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
    target
        .getContent()
        .forEach(
            alarm -> {
              assertTrue(emUtil.isLoaded(alarm, "listener"));
              assertTrue(emUtil.isLoaded(alarm, "targetReview"));
              assertTrue(emUtil.isLoaded(alarm, "targetProduct"));
              assertTrue(emUtil.isLoaded(alarm, "targetRefund"));
            });
  }

  public void checkWhere_retrieveAllByMember(long alarmListenerId, Slice<Alarm> target) {
    target
        .getContent()
        .forEach(alarm -> assertEquals(alarmListenerId, alarm.getListener().getId()));
  }
}
