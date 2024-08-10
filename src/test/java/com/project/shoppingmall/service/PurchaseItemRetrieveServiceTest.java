package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseItemRetrieveRepository;
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
  private MemberService mockMemberService;
  private ProductService mockProductService;

  @BeforeEach
  public void beforeEach() {
    mockPurchaseItemRetrieveRepository = mock(PurchaseItemRetrieveRepository.class);
    mockMemberService = mock(MemberService.class);
    mockProductService = mock(ProductService.class);
    target =
        new PurchaseItemRetrieveService(
            mockPurchaseItemRetrieveRepository, mockMemberService, mockProductService);
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
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
}
