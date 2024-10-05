package com.project.shoppingmall.service.refund;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.*;
import com.project.shoppingmall.repository.RefundRepository;
import com.project.shoppingmall.service.alarm.AlarmService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.purchase.PurchaseBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.refund.RefundBuilder;
import com.project.shoppingmall.test_entity.refund.RefundChecker;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.type.RefundStateType;
import com.project.shoppingmall.type.RefundStateTypeForPurchaseItem;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class RefundServiceTest {
  private RefundService target;
  private RefundRepository mockRefundRepository;
  private RefundFindService mockRefundFindService;
  private MemberFindService mockMemberFindService;
  private PurchaseItemFindService mockPurchaseItemFindService;
  private PurchaseItemService mockPurchaseItemService;
  private IamportClient mockIamportClient;
  private AlarmService mockAlarmService;
  private Integer givenRefundPossibleDate = 30;

  @BeforeEach
  public void beforeEach() {
    mockRefundRepository = mock(RefundRepository.class);
    mockRefundFindService = mock(RefundFindService.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockPurchaseItemFindService = mock(PurchaseItemFindService.class);
    mockPurchaseItemService = mock(PurchaseItemService.class);
    mockIamportClient = mock(IamportClient.class);
    mockAlarmService = mock(AlarmService.class);
    target =
        new RefundService(
            mockRefundRepository,
            mockRefundFindService,
            mockMemberFindService,
            mockPurchaseItemFindService,
            mockPurchaseItemService,
            mockIamportClient,
            mockAlarmService);

    ReflectionTestUtils.setField(target, "refundSavePossibleDate", givenRefundPossibleDate);
  }

  @Test
  @DisplayName("saveRefund() : 정상흐름")
  public void saveRefund_ok() {
    // given
    Long inputMemberId = 20L;
    Long inputPurchaseItemId = 23L;
    String inputRequestTitle = "testRefundRequestTitle";
    String inputRequestContent = "testRefundRequestContent";

    Member givenRequester = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L);
    LocalDateTime givenPurchaseDate = LocalDateTime.now().minusDays(givenRefundPossibleDate - 1);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct, givenPurchaseDate);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenRequester, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Long saveRefundId = 12314L;

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockPurchaseItemFindService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockPurchaseItemService.refundIsPossible(any())).thenReturn(true);
    set_refundRepository_save(saveRefundId);

    // when
    Refund result =
        target.saveRefund(
            inputMemberId, inputPurchaseItemId, inputRequestTitle, inputRequestContent);

    // then
    RefundChecker.checkRequestSateRefund(
        givenPurchaseItem, inputRequestTitle, inputRequestContent, result);
    checkRegisterRefundAtPurchaseItem(givenPurchaseItem);
    check_alarmService_makeRefundRequestAlarm(saveRefundId);
  }

  @Test
  @DisplayName("saveRefund() : 결제되지 않은 구매에 대한 환불 요청")
  public void saveRefund_noPayment() {
    // given
    Long inputMemberId = 20L;
    Long inputPurchaseItemId = 23L;
    String inputRequestTitle = "testRefundRequestTitle";
    String inputRequestContent = "testRefundRequestContent";

    Member givenRequester = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L);
    LocalDateTime givenPurchaseDate = LocalDateTime.now().minusDays(givenRefundPossibleDate - 1);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct, givenPurchaseDate);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenRequester, List.of(givenPurchaseItem), PurchaseStateType.FAIL);
    Long saveRefundId = 12314L;

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockPurchaseItemFindService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockPurchaseItemService.refundIsPossible(any())).thenReturn(true);
    set_refundRepository_save(saveRefundId);

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.saveRefund(
                inputMemberId, inputPurchaseItemId, inputRequestTitle, inputRequestContent));
  }

  @Test
  @DisplayName("saveRefund() : 다른 회원의 구매에 대한 환불 요청")
  public void saveRefund_otherMemberPurchaseItem() {
    // given
    Long inputMemberId = 20L;
    Long inputPurchaseItemId = 23L;
    String inputRequestTitle = "testRefundRequestTitle";
    String inputRequestContent = "testRefundRequestContent";

    Member givenRequester = MemberBuilder.makeMember(inputMemberId);
    Member otherMember = MemberBuilder.makeMember(503021L);
    Product givenProduct = ProductBuilder.makeProduct(3210L);
    LocalDateTime givenPurchaseDate = LocalDateTime.now().minusDays(givenRefundPossibleDate - 1);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct, givenPurchaseDate);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, otherMember, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Long saveRefundId = 12314L;

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockPurchaseItemFindService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockPurchaseItemService.refundIsPossible(any())).thenReturn(true);
    set_refundRepository_save(saveRefundId);

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.saveRefund(
                inputMemberId, inputPurchaseItemId, inputRequestTitle, inputRequestContent));
  }

  @Test
  @DisplayName("saveRefund() : 완료되었거나 진행중인 환불이 이미 존재")
  public void saveRefund_alreadyProcessedOrCompleteRefund() {
    // given
    Long inputMemberId = 20L;
    Long inputPurchaseItemId = 23L;
    String inputRequestTitle = "testRefundRequestTitle";
    String inputRequestContent = "testRefundRequestContent";

    Member givenRequester = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L);
    LocalDateTime givenPurchaseDate = LocalDateTime.now().minusDays(givenRefundPossibleDate - 1);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct, givenPurchaseDate);
    Refund givenCompleteRefund =
        RefundBuilder.make(87634L, RefundStateType.COMPLETE, givenPurchaseItem);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenRequester, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Long saveRefundId = 12314L;

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockPurchaseItemFindService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockPurchaseItemService.refundIsPossible(any())).thenReturn(false);
    set_refundRepository_save(saveRefundId);

    // when
    assertThrows(
        ProcessOrCompleteRefund.class,
        () ->
            target.saveRefund(
                inputMemberId, inputPurchaseItemId, inputRequestTitle, inputRequestContent));
  }

  @Test
  @DisplayName("saveRefund() : 환불요청 기간이 지난 구매아이템에 대해 환불요청을 시도")
  public void saveRefund_alreadyPassedRefundPossibleDay() {
    // given
    Long inputMemberId = 20L;
    Long inputPurchaseItemId = 23L;
    String inputRequestTitle = "testRefundRequestTitle";
    String inputRequestContent = "testRefundRequestContent";

    Member givenRequester = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L);
    LocalDateTime givenPurchaseDate = LocalDateTime.now().minusDays(givenRefundPossibleDate + 1);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct, givenPurchaseDate);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenRequester, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Long saveRefundId = 12314L;

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenRequester));
    when(mockPurchaseItemFindService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockPurchaseItemService.refundIsPossible(any())).thenReturn(true);
    set_refundRepository_save(saveRefundId);

    // when
    assertThrows(
        PassedRefundRequest.class,
        () ->
            target.saveRefund(
                inputMemberId, inputPurchaseItemId, inputRequestTitle, inputRequestContent));
  }

  @Test
  @DisplayName("acceptRefund() : 정상흐름")
  public void acceptRefund_ok() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;
    String inputResponseMessage = "testMessage";

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.REQUEST, givenPurchaseItem);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when
    Refund result = target.acceptRefund(inputMemberId, inputRefundId, inputResponseMessage);

    // then
    RefundChecker.checkAcceptStateRefund(givenPurchaseItem, inputResponseMessage, result);
  }

  @Test
  @DisplayName("acceptRefund() : 자신이 판매하지 않는 제품에 대한 환불데이터 승인요청")
  public void acceptRefund_otherSellerRefund() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;
    String inputResponseMessage = "testMessage";

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Member givenOtherSeller = MemberBuilder.makeMember(50302L);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenOtherSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.REQUEST, givenPurchaseItem);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when then
    assertThrows(
        DataNotFound.class,
        () -> target.acceptRefund(inputMemberId, inputRefundId, inputResponseMessage));
  }

  @Test
  @DisplayName("acceptRefund() : request상태가 아닌 환불에 해당 승인요청")
  public void acceptRefund_noRequest() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;
    String inputResponseMessage = "testMessage";

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.COMPLETE, givenPurchaseItem);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when then
    assertThrows(
        NotRequestStateRefund.class,
        () -> target.acceptRefund(inputMemberId, inputRefundId, inputResponseMessage));
  }

  @Test
  @DisplayName("rejectRefund() : 정상흐름")
  public void rejectRefund_ok() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;
    String inputResponseMessage = "testMessage";

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.REQUEST, givenPurchaseItem);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when
    Refund result = target.rejectRefund(inputMemberId, inputRefundId, inputResponseMessage);

    // then
    RefundChecker.checkRejectedStateRefund(givenPurchaseItem, inputResponseMessage, result);
  }

  @Test
  @DisplayName("rejectRefund() : 자신이 판매하지 않는 제품에 대한 환불데이터 반려요청")
  public void rejectRefund_otherSellerRefund() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;
    String inputResponseMessage = "testMessage";

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Member otherSeller = MemberBuilder.makeMember(123125L);
    Product givenProduct = ProductBuilder.makeProduct(3210L, otherSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.REQUEST, givenPurchaseItem);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.rejectRefund(inputMemberId, inputRefundId, inputResponseMessage));
  }

  @Test
  @DisplayName("rejectRefund() : request상태가 아닌 환불에 해당 반려요청")
  public void rejectRefund_noRequest() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;
    String inputResponseMessage = "testMessage";

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.COMPLETE, givenPurchaseItem);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when
    assertThrows(
        NotRequestStateRefund.class,
        () -> target.rejectRefund(inputMemberId, inputRefundId, inputResponseMessage));
  }

  @Test
  @DisplayName("completeRefund() : 정상흐름")
  public void completeRefund_ok() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.ACCEPT, givenPurchaseItem);
    IamportResponse mockIamportResponse = setMockIamportClientRefundResponse();

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));
    when(mockIamportClient.cancelPaymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when
    Refund result = target.completeRefund(inputMemberId, inputRefundId);

    // then
    RefundChecker.checkCompleteStateRefund(givenPurchaseItem, givenRefund);
    check_imaportClient_cancelPaymentByImpUid(givenPurchaseItem);
  }

  @Test
  @DisplayName("completeRefund() : 다른 판매자의 상품에 대한 환불 완료요청")
  public void completeRefund_otherSellerRefund() {
    long inputMemberId = 10L;
    long inputRefundId = 20L;

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Member otherSeller = MemberBuilder.makeMember(4213L);
    Product givenProduct = ProductBuilder.makeProduct(3210L, otherSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.ACCEPT, givenPurchaseItem);
    IamportResponse mockIamportResponse = setMockIamportClientRefundResponse();

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));
    when(mockIamportClient.cancelPaymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when then
    assertThrows(DataNotFound.class, () -> target.completeRefund(inputMemberId, inputRefundId));
  }

  @Test
  @DisplayName("completeRefund() : Accept상태가 아닌 환불에 해당 완료요청")
  public void completeRefund_noAccept() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.REJECTED, givenPurchaseItem);
    IamportResponse mockIamportResponse = setMockIamportClientRefundResponse();

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));
    when(mockIamportClient.cancelPaymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when then
    assertThrows(
        NotAcceptStateRefund.class, () -> target.completeRefund(inputMemberId, inputRefundId));
  }

  @Test
  @DisplayName("completeRefund() : 정해진 금액보다 많은 금액의 환불 요청")
  public void completeRefund_wrongRefund() {
    // given
    long inputMemberId = 10L;
    long inputRefundId = 20L;

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(3210L, givenSeller);
    Member givenBuyer = MemberBuilder.makeMember(31232L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(51231L, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            4123L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    Refund givenRefund =
        RefundBuilder.make(inputRefundId, RefundStateType.ACCEPT, givenPurchaseItem);
    IamportResponse mockIamportResponse = setMockIamportClientEmptyRefundResponse();

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));
    when(mockIamportClient.cancelPaymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when then
    assertThrows(
        FailRefundException.class, () -> target.completeRefund(inputMemberId, inputRefundId));
  }

  public IamportResponse setMockIamportClientRefundResponse() {
    IamportResponse mockiamportResponse = mock(IamportResponse.class);
    when(mockiamportResponse.getResponse()).thenReturn(mock(Payment.class));
    return mockiamportResponse;
  }

  public IamportResponse setMockIamportClientEmptyRefundResponse() {
    IamportResponse mockiamportResponse = mock(IamportResponse.class);
    when(mockiamportResponse.getResponse()).thenReturn(null);
    return mockiamportResponse;
  }

  public void set_refundRepository_save(long savedRefundId) {
    when(mockRefundRepository.save(any(Refund.class)))
        .thenAnswer(
            invocation -> {
              Refund refund = invocation.getArgument(0);
              ReflectionTestUtils.setField(refund, "id", savedRefundId);
              return refund;
            });
  }

  public void checkRegisterRefundAtPurchaseItem(PurchaseItem target) {
    assertEquals(1, target.getRefunds().size());
    assertFalse(target.getIsRefund());
    assertNotNull(target.getFinalRefundCreatedDate());
    assertEquals(RefundStateTypeForPurchaseItem.REQUEST, target.getFinalRefundState());
  }

  public void check_alarmService_makeRefundRequestAlarm(long refundId) {
    ArgumentCaptor<Long> refundIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmService, times(1)).makeRefundRequestAlarm(refundIdCaptor.capture());
    assertEquals(refundId, refundIdCaptor.getValue());
  }

  public void check_imaportClient_cancelPaymentByImpUid(PurchaseItem purchaseItem) {
    ArgumentCaptor<CancelData> cancelDataCaptor = ArgumentCaptor.forClass(CancelData.class);
    verify(mockIamportClient, times(1)).cancelPaymentByImpUid(cancelDataCaptor.capture());

    assertEquals(
        purchaseItem.getPurchase().getPaymentUid(),
        ReflectionTestUtils.getField(cancelDataCaptor.getValue(), "imp_uid"));
    int realRefundPrice =
        ((BigDecimal) ReflectionTestUtils.getField(cancelDataCaptor.getValue(), "amount"))
            .intValue();
    assertEquals(purchaseItem.getFinalPrice(), realRefundPrice);
  }
}
