package com.project.shoppingmall.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.product.ProductHeaderDto;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.repository.ProductRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_dto.SliceResultManager;
import com.project.shoppingmall.test_dto.product.ProductHeaderDtoManager;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.product_type.ProductTypeBuilder;
import com.project.shoppingmall.type.ProductRetrieveFilterType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

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
    Long inputProductTypeId = 6L;
    int inputSliceSize = 5;
    int inputSliceNum = 10;
    ProductRetrieveFilterType inputFilterType = ProductRetrieveFilterType.LATEST;

    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputProductTypeId, "test$test");
    List<Product> givenProducts =
        ProductBuilder.makeProductList(List.of(1L, 2L, 3L), givenProductType);
    Slice<Product> givenSliceResult =
        SliceManager.setMockSlice(inputSliceNum, inputSliceSize, givenProducts);

    when(mockProductRetrieveRepository.findByProductType(anyLong(), any()))
        .thenReturn(givenSliceResult);

    // when
    SliceResult<ProductHeaderDto> result =
        target.retrieveByTypeWithFilter(
            inputProductTypeId, inputSliceSize, inputSliceNum, inputFilterType);

    // then
    check_productRetrieveRepository_findByProductType(
        inputProductTypeId, inputSliceSize, inputSliceNum, Sort.Direction.DESC, "createDate");
    SliceResultManager.checkOnlySliceData(givenSliceResult, result);
    ProductHeaderDtoManager.checkList(givenProducts, result.getContentList());
  }

  @Test
  @DisplayName("retrieveByTypeWithFilter() : 정상흐름2")
  public void retrieveByTypeWithFilter_ok2() {
    // given
    Long inputProductTypeId = 6L;
    int inputSliceSize = 5;
    int inputSliceNum = 10;
    ProductRetrieveFilterType inputFilterType = ProductRetrieveFilterType.LOW_PRICE;

    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputProductTypeId, "test$test");
    List<Product> givenProducts =
        ProductBuilder.makeProductList(List.of(1L, 2L, 3L), givenProductType);
    Slice<Product> givenSliceResult =
        SliceManager.setMockSlice(inputSliceNum, inputSliceSize, givenProducts);

    when(mockProductRetrieveRepository.findByProductType(anyLong(), any()))
        .thenReturn(givenSliceResult);

    // when
    SliceResult<ProductHeaderDto> result =
        target.retrieveByTypeWithFilter(
            inputProductTypeId, inputSliceSize, inputSliceNum, inputFilterType);

    // then
    check_productRetrieveRepository_findByProductType(
        inputProductTypeId, inputSliceSize, inputSliceNum, Sort.Direction.ASC, "finalPrice");
    SliceResultManager.checkOnlySliceData(givenSliceResult, result);
    ProductHeaderDtoManager.checkList(givenProducts, result.getContentList());
  }

  @Test
  @DisplayName("retrieveBySearchWordWithFilter() : 정상흐름1")
  public void retrieveBySearchWordWithFilter_ok1() {
    // given
    String inputSearchWord = "test";
    int inputSliceSize = 5;
    int inputSliceNum = 10;
    ProductRetrieveFilterType inputFilterType = ProductRetrieveFilterType.LOW_SCORE;

    List<Product> givenProducts =
        ProductBuilder.makeProductList(List.of(1L, 2L, 3L), inputSearchWord);
    Slice<Product> givenSliceResult =
        SliceManager.setMockSlice(inputSliceNum, inputSliceSize, givenProducts);

    when(mockProductRetrieveRepository.findBySearchWord(anyString(), any()))
        .thenReturn(givenSliceResult);

    // when
    SliceResult<ProductHeaderDto> result =
        target.retrieveBySearchWordWithFilter(
            inputSearchWord, inputSliceSize, inputSliceNum, inputFilterType);

    // then
    check_productRetrieveRepository_findBySearchWord(
        inputSearchWord, inputSliceSize, inputSliceNum, Sort.Direction.ASC, "scoreAvg");
    SliceResultManager.checkOnlySliceData(givenSliceResult, result);
    ProductHeaderDtoManager.checkList(givenProducts, result.getContentList());
  }

  @Test
  @DisplayName("retrieveBySearchWordWithFilter() : 정상흐름2")
  public void retrieveBySearchWordWithFilter_ok2() {
    // given
    String inputSearchWord = "test";
    int inputSliceSize = 5;
    int inputSliceNum = 10;
    ProductRetrieveFilterType inputFilterType = ProductRetrieveFilterType.OLDEST;

    List<Product> givenProducts =
        ProductBuilder.makeProductList(List.of(1L, 2L, 3L), inputSearchWord);
    Slice<Product> givenSliceResult =
        SliceManager.setMockSlice(inputSliceNum, inputSliceSize, givenProducts);

    when(mockProductRetrieveRepository.findBySearchWord(anyString(), any()))
        .thenReturn(givenSliceResult);

    // when
    SliceResult<ProductHeaderDto> result =
        target.retrieveBySearchWordWithFilter(
            inputSearchWord, inputSliceSize, inputSliceNum, inputFilterType);

    // then
    check_productRetrieveRepository_findBySearchWord(
        inputSearchWord, inputSliceSize, inputSliceNum, Sort.Direction.ASC, "createDate");
    SliceResultManager.checkOnlySliceData(givenSliceResult, result);
    ProductHeaderDtoManager.checkList(givenProducts, result.getContentList());
  }

  @Test
  @DisplayName("retrieveBySeller() : 정상흐름")
  public void retrieveBySeller_ok() {
    // given
    long inputSellerId = 10L;
    int inputSliceNumber = 0;
    int inputSliceSize = 10;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId);
    List<Product> givenProductList =
        ProductBuilder.makeProductList(List.of(1L, 2L, 3L), givenSeller);
    Slice<Product> givenSlice =
        SliceManager.setMockSlice(inputSliceNumber, inputSliceSize, givenProductList);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenSeller));
    when(mockProductRetrieveRepository.findAllBySeller(anyLong(), any())).thenReturn(givenSlice);

    // when
    SliceResult<ProductHeaderDto> result =
        target.retrieveBySeller(inputSellerId, inputSliceNumber, inputSliceSize);

    // then
    check_productRetrieveRepository_findAllBySeller(
        inputSellerId, inputSliceNumber, inputSliceSize);
    SliceResultManager.checkOnlySliceData(givenSlice, result);
    ProductHeaderDtoManager.checkList(givenProductList, result.getContentList());
  }

  @Test
  @DisplayName("retrieveByRandom : 정상흐름()")
  public void retrieveByRandom_ok() {
    // given
    int inputSliceNumber = 20;
    int inputSliceSize = 30;

    List<Product> givenProductList = ProductBuilder.makeProductList(List.of(1L, 2L, 3L));
    Slice<Product> givenSlice =
        SliceManager.setMockSlice(inputSliceNumber, inputSliceSize, givenProductList);
    when(mockProductRetrieveRepository.findAllByRandom(any())).thenReturn(givenSlice);

    // when
    target.retrieveByRandom(inputSliceNumber, inputSliceSize);

    // then
    check_productRetrieveRepository_findAllByRandom(inputSliceNumber, inputSliceSize);
  }

  public void check_productRetrieveRepository_findByProductType(
      long inputProductTypeId,
      long inputSliceSize,
      long inputSliceNum,
      Sort.Direction sortDirection,
      String sortColumn) {
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findByProductType(productTypeIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(inputProductTypeId, productTypeIdCaptor.getValue());
    assertEquals(inputSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(inputSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        sortDirection,
        pageRequestCaptor.getValue().getSort().getOrderFor(sortColumn).getDirection());
    assertEquals(
        sortColumn, pageRequestCaptor.getValue().getSort().getOrderFor(sortColumn).getProperty());
  }

  public void check_productRetrieveRepository_findBySearchWord(
      String inputSearchWord,
      long inputSliceSize,
      long inputSliceNum,
      Sort.Direction sortDirection,
      String sortColumn) {
    ArgumentCaptor<String> searchWordCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findBySearchWord(searchWordCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(inputSearchWord, searchWordCaptor.getValue());
    assertEquals(inputSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(inputSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        sortDirection,
        pageRequestCaptor.getValue().getSort().getOrderFor(sortColumn).getDirection());
    assertEquals(
        sortColumn, pageRequestCaptor.getValue().getSort().getOrderFor(sortColumn).getProperty());
  }

  public void check_productRetrieveRepository_findAllBySeller(
      long inputSellerId, long inputSliceNumber, long inputSliceSize) {
    ArgumentCaptor<Long> sellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findAllBySeller(sellerIdCaptor.capture(), pageRequestCaptor.capture());

    assertSame(inputSellerId, sellerIdCaptor.getValue());
    assertEquals(inputSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(inputSliceNumber, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.DESC,
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate",
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getProperty());
  }

  public void check_productRetrieveRepository_findAllByRandom(
      long inputSliceNumber, long inputSliceSize) {
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1)).findAllByRandom(pageRequestCaptor.capture());
    assertEquals(inputSliceNumber, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(inputSliceSize, pageRequestCaptor.getValue().getPageSize());
  }
}
