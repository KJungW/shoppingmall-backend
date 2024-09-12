package com.project.shoppingmall.service.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.CannotDeleteMemberByRefund;
import com.project.shoppingmall.exception.CannotDeleteMemberBySellingRecord;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import com.project.shoppingmall.service.alarm.AlarmFindService;
import com.project.shoppingmall.service.basket_item.BasketItemDeleteService;
import com.project.shoppingmall.service.basket_item.BasketItemFindService;
import com.project.shoppingmall.service.chat_room.ChatRoomDeleteService;
import com.project.shoppingmall.service.chat_room.ChatRoomFindService;
import com.project.shoppingmall.service.product.ProductDeleteService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.service.refund.RefundFindService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportFindService;
import com.project.shoppingmall.service.review.ReviewDeleteService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.PurchaseItemBuilder;
import com.project.shoppingmall.testdata.RefundBuilder;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.RefundStateType;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberDeleteServiceTest {
  @InjectMocks private MemberDeleteService target;
  @Mock private MemberRepository mockMemberRepository;
  @Mock private MemberFindService mockMemberFindService;
  @Mock private ReportFindService mockReportFindService;
  @Mock private ReportDeleteService mockReportDeleteService;
  @Mock private BasketItemFindService mockBasketItemFindService;
  @Mock private BasketItemDeleteService mockBasketItemDeleteService;
  @Mock private ProductFindService mockProductFindService;
  @Mock private ProductDeleteService mockProductDeleteService;
  @Mock private AlarmFindService mockAlarmFindService;
  @Mock private AlarmDeleteService mockAlarmDeleteService;
  @Mock private ReviewFindService mockReviewFindService;
  @Mock private ReviewDeleteService mockReviewDeleteService;
  @Mock private ChatRoomFindService mockChatRoomFindService;
  @Mock private ChatRoomDeleteService mockChatRoomDeleteService;
  @Mock private PurchaseItemFindService mockPurchaseItemFindService;
  @Mock private RefundFindService mockRefundFindService;
  public Integer refundPossibleDay = 30;

  @BeforeEach
  public void beforeEach() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(target, "refundPossibleDay", refundPossibleDay);
  }

  @Test
  @DisplayName("deleteMemberInController() : 정상흐름")
  public void deleteMemberInController_ok() {
    // given
    long inputMemberId = 10L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(
            42L, givenMember, LocalDateTime.now().minusDays(refundPossibleDay + 1));
    when(mockPurchaseItemFindService.findLatestBySeller(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockRefundFindService.findAllProcessingStateRefundBySeller(anyLong()))
        .thenReturn(new ArrayList<>());

    // when
    assertDoesNotThrow(() -> target.deleteMemberInController(inputMemberId));
  }

  @Test
  @DisplayName("deleteMemberInController() : 환불기간이 지나지 않은 판매기록이 존재")
  public void deleteMemberInController_refundPossibleSellingRecord() {
    // given
    long inputMemberId = 10L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(
            42L, givenMember, LocalDateTime.now().minusDays(refundPossibleDay - 15));
    when(mockPurchaseItemFindService.findLatestBySeller(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockRefundFindService.findAllProcessingStateRefundBySeller(anyLong()))
        .thenReturn(new ArrayList<>());

    // when
    assertThrows(
        CannotDeleteMemberBySellingRecord.class,
        () -> target.deleteMemberInController(inputMemberId));
  }

  @Test
  @DisplayName("deleteMemberInController() : 처리되지 않은 환불요청이 존재")
  public void deleteMemberInController_notProcessingRefundRequest() throws IOException {
    // given
    long inputMemberId = 10L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(
            42L, givenMember, LocalDateTime.now().minusDays(refundPossibleDay + 1));
    when(mockPurchaseItemFindService.findLatestBySeller(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    List<Refund> givenNotProcessingRefund =
        List.of(
            RefundBuilder.makeRefund(RefundStateType.REQUEST, givenPurchaseItem),
            RefundBuilder.makeRefund(RefundStateType.ACCEPT, givenPurchaseItem));
    when(mockRefundFindService.findAllProcessingStateRefundBySeller(anyLong()))
        .thenReturn(givenNotProcessingRefund);

    // when
    assertThrows(
        CannotDeleteMemberByRefund.class, () -> target.deleteMemberInController(inputMemberId));
  }
}
