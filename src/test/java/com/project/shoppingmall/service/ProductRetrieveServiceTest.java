package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.repository.ProductRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductRetrieveService;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.type.ProductRetrieveFilterType;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

class ProductRetrieveServiceTest {
  private ProductRetrieveService target;
  private ProductRetrieveRepository mockProductRetrieveRepository;
  private MemberFindService mockMemberFindService;

  @BeforeEach
  public void beforeEach() {
    mockProductRetrieveRepository = mock(ProductRetrieveRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    target = new ProductRetrieveService(mockProductRetrieveRepository, mockMemberFindService);
  }

  @Test
  @DisplayName("retrieveByTypeWithFilter() : 정상흐름1")
  public void retrieveByTypeWithFilter_ok1() {
    // given
    Long givenProductTypeId = 6L;
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.LATEST;

    Slice mockSliceResult = mock(Slice.class);
    when(mockSliceResult.getContent()).thenReturn(new ArrayList<>());
    when(mockProductRetrieveRepository.findByProductType(anyLong(), any()))
        .thenReturn(mockSliceResult);

    // when
    target.retrieveByTypeWithFilter(
        givenProductTypeId, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findByProductType(productTypeIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeId, productTypeIdCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.DESC,
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate",
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveByTypeWithFilter() : 정상흐름2")
  public void retrieveByTypeWithFilter_ok2() {
    // given
    Long givenProductTypeId = 6L;
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.LOW_PRICE;

    Slice mockSliceResult = mock(Slice.class);
    when(mockSliceResult.getContent()).thenReturn(new ArrayList<>());
    when(mockProductRetrieveRepository.findByProductType(anyLong(), any()))
        .thenReturn(mockSliceResult);

    // when
    target.retrieveByTypeWithFilter(
        givenProductTypeId, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findByProductType(productTypeIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeId, productTypeIdCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.ASC,
        pageRequestCaptor.getValue().getSort().getOrderFor("finalPrice").getDirection());
    assertEquals(
        "finalPrice",
        pageRequestCaptor.getValue().getSort().getOrderFor("finalPrice").getProperty());
  }

  @Test
  @DisplayName("retrieveBySearchWordWithFilter() : 정상흐름1")
  public void retrieveBySearchWordWithFilter_ok1() {
    // given
    String givenSearchWord = "testWord";
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.LOW_SCORE;

    Slice mockSliceResult = mock(Slice.class);
    when(mockSliceResult.getContent()).thenReturn(new ArrayList<>());
    when(mockProductRetrieveRepository.findBySearchWord(anyString(), any()))
        .thenReturn(mockSliceResult);

    // when
    target.retrieveBySearchWordWithFilter(
        givenSearchWord, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<String> searchWordCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findBySearchWord(searchWordCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenSearchWord, searchWordCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.ASC,
        pageRequestCaptor.getValue().getSort().getOrderFor("scoreAvg").getDirection());
    assertEquals(
        "scoreAvg", pageRequestCaptor.getValue().getSort().getOrderFor("scoreAvg").getProperty());
  }

  @Test
  @DisplayName("retrieveByTypeWithFilter() : 정상흐름2")
  public void retrieveBySearchWordWithFilter_ok2() {
    // given
    String givenSearchWord = "testWord";
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.OLDEST;

    Slice mockSliceResult = mock(Slice.class);
    when(mockSliceResult.getContent()).thenReturn(new ArrayList<>());
    when(mockProductRetrieveRepository.findBySearchWord(anyString(), any()))
        .thenReturn(mockSliceResult);

    // when
    target.retrieveBySearchWordWithFilter(
        givenSearchWord, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<String> searchWordCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findBySearchWord(searchWordCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenSearchWord, searchWordCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.ASC,
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate",
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveBySeller() : 정상흐름")
  public void retrieveBySeller_ok() {
    // given
    long givenSellerId = 10L;
    int givenSliceNumber = 0;
    int givenSliceSize = 10;

    Member givenSeller = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenSeller, "id", givenSellerId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenSeller));

    Slice mockSliceResult = mock(Slice.class);
    when(mockSliceResult.getContent()).thenReturn(new ArrayList());
    when(mockProductRetrieveRepository.findAllBySeller(anyLong(), any()))
        .thenReturn(mockSliceResult);

    // when
    target.retrieveBySeller(givenSellerId, givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<Long> sellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findAllBySeller(sellerIdCaptor.capture(), pageRequestCaptor.capture());

    assertSame(givenSellerId, sellerIdCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNumber, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.DESC,
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate",
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveByRandom : 정상흐름()")
  public void retrieveByRandom_ok() {
    // given
    int givenSliceNumber = 20;
    int givenSliceSize = 30;

    Slice mockSliceResult = mock(Slice.class);
    when(mockSliceResult.getContent()).thenReturn(new ArrayList());
    when(mockProductRetrieveRepository.findAllByRandom(any())).thenReturn(mockSliceResult);

    // when
    target.retrieveByRandom(givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1)).findAllByRandom(pageRequestCaptor.capture());
    assertEquals(givenSliceNumber, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
  }
}
