package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseItemRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemRetrieveService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

class PurchaseItemRetrieveServiceTest {
  private PurchaseItemRetrieveService target;
  private PurchaseItemRetrieveRepository mockPurchaseItemRetrieveRepository;
  private MemberFindService mockMemberFindService;
  private ProductService mockProductService;

  @BeforeEach
  public void beforeEach() {
    mockPurchaseItemRetrieveRepository = mock(PurchaseItemRetrieveRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockProductService = mock(ProductService.class);
    target =
        new PurchaseItemRetrieveService(
            mockPurchaseItemRetrieveRepository, mockMemberFindService, mockProductService);
  }

  @Test
  @DisplayName("retrieveAllByProduct() : 정상흐름")
  public void retrieveAllByProduct_ok() throws IOException {
    // given
    long givenMemberId = 10L;
    long givenProductId = 20L;
    int givenSliceNumber = 0;
    int givenSliceSize = 5;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    Product givenProduct = ProductBuilder.fullData().seller(givenMember).build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    when(mockProductService.findById(any())).thenReturn(Optional.of(givenProduct));

    // when
    target.retrieveAllForSeller(givenMemberId, givenProductId, givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findAllForSeller(productIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductId, productIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNumber, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveAllByProduct() : 다른 회원의 판매 제품에 대한 구매목록 조회 시도")
  public void retrieveAllByProduct_otherMemberProduct() throws IOException {
    // given
    long givenMemberId = 10L;
    long givenProductId = 20L;
    int givenSliceNumber = 0;
    int givenSliceSize = 5;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    long wrongMemberId = 2L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", wrongMemberId);
    when(mockProductService.findById(any())).thenReturn(Optional.of(givenProduct));

    System.out.println("givenMember.getId() = " + givenMember.getId());
    System.out.println("givenProduct.getSeller().getId() = " + givenProduct.getSeller().getId());

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.retrieveAllForSeller(
                givenMemberId, givenProductId, givenSliceNumber, givenSliceSize));
  }

  @Test
  @DisplayName("retrieveRefundedAllForBuyer() : 정상흐름'")
  public void retrieveRefundedAllForBuyer_ok() {
    // given
    long givenBuyerId = 30L;
    int givenSliceNumber = 1;
    int givenSliceSize = 10;

    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenBuyerId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenBuyer));

    // when
    target.retrieveRefundedAllForBuyer(givenBuyerId, givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<Long> buyerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findRefundedAllForBuyer(buyerIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenBuyerId, buyerIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNumber, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("finalRefundCreatedDate").getDirection());
    assertEquals(
        "finalRefundCreatedDate",
        captoredPageRequest.getSort().getOrderFor("finalRefundCreatedDate").getProperty());
  }

  @Test
  @DisplayName("retrieveRefundedAllForSeller() : 정상흐름")
  public void retrieveRefundedAllForSeller_ok() {
    // given
    long givenSellerId = 30L;
    int givenSliceNumber = 1;
    int givenSliceSize = 10;

    Member givenSeller = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenSeller, "id", givenSellerId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));

    // when
    target.retrieveRefundedAllForSeller(givenSellerId, givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<Long> sellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findRefundedAllForSeller(sellerIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenSellerId, sellerIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNumber, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("finalRefundCreatedDate").getDirection());
    assertEquals(
        "finalRefundCreatedDate",
        captoredPageRequest.getSort().getOrderFor("finalRefundCreatedDate").getProperty());
  }
}
