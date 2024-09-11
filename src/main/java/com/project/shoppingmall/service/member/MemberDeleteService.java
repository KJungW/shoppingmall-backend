package com.project.shoppingmall.service.member;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.CannotDeleteMemberByPassword;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import com.project.shoppingmall.service.alarm.AlarmFindService;
import com.project.shoppingmall.service.basket_item.BasketItemDeleteService;
import com.project.shoppingmall.service.basket_item.BasketItemFindService;
import com.project.shoppingmall.service.chat_room.ChatRoomDeleteService;
import com.project.shoppingmall.service.chat_room.ChatRoomFindService;
import com.project.shoppingmall.service.product.ProductDeleteService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportFindService;
import com.project.shoppingmall.service.review.ReviewDeleteService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.util.PasswordEncoderUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberDeleteService {
  private final MemberRepository memberRepository;
  private final MemberFindService memberFindService;
  private final ReportFindService reportFindService;
  private final ReportDeleteService reportDeleteService;
  private final BasketItemFindService basketItemFindService;
  private final BasketItemDeleteService basketItemDeleteService;
  private final ProductFindService productFindService;
  private final ProductDeleteService productDeleteService;
  private final AlarmFindService alarmFindService;
  private final AlarmDeleteService alarmDeleteService;
  private final ReviewFindService reviewFindService;
  private final ReviewDeleteService reviewDeleteService;
  private final ChatRoomFindService chatRoomFindService;
  private final ChatRoomDeleteService chatRoomDeleteService;

  public void deleteMember(Member member) {
    // 해당 회원의 신고 삭제
    List<ProductReport> productReports =
        reportFindService.findAllProductReportByReporter(member.getId());
    reportDeleteService.deleteProductReportList(productReports);
    List<ReviewReport> reviewReports =
        reportFindService.findAllReviewReportByReporter(member.getId());
    reportDeleteService.deleteReviewReportList(reviewReports);

    // 장바구니 삭제
    List<BasketItem> basketItems = basketItemFindService.findAllByMember(member.getId());
    basketItemDeleteService.deleteBasketItemList(basketItems);

    // 알림삭제
    List<Alarm> alarms = alarmFindService.findAllByListener(member.getId());
    alarmDeleteService.deleteAlarmList(alarms);

    // 리뷰 삭제
    List<Review> reviews = reviewFindService.findAllByWriter(member.getId());
    reviewDeleteService.deleteReviewList(reviews);

    // 채팅방 삭제
    List<ChatRoom> chatRoomsByBuyer = chatRoomFindService.findAllByBuyer(member.getId());
    chatRoomDeleteService.deleteChatRoomList(chatRoomsByBuyer);
    List<ChatRoom> chatRoomsBySeller = chatRoomFindService.findAllBySeller(member.getId());
    chatRoomDeleteService.deleteChatRoomList(chatRoomsBySeller);

    // 제품삭제
    List<Product> products = productFindService.findAllBySeller(member.getId());
    productDeleteService.deleteProductList(products);

    // 회원삭제
    memberRepository.delete(member);
  }

  public void deleteMemberInController(long memberId, String password) {
    Member member =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    if (!PasswordEncoderUtil.checkPassword(password, member.getPassword()))
      throw new CannotDeleteMemberByPassword("회원의 비밀번호가 맞지 않습니다.");
    deleteMember(member);
  }
}
