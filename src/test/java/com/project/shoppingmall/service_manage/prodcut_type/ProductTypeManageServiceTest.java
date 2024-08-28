package com.project.shoppingmall.service_manage.prodcut_type;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.exception.CannotDeleteBaseProductType;
import com.project.shoppingmall.exception.CannotUpdateBaseProductType;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.final_value.FinalValue;
import com.project.shoppingmall.repository.ProductTypeRepository;
import com.project.shoppingmall.service.EntityManagerService;
import com.project.shoppingmall.service.alarm.AlarmService;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import com.project.shoppingmall.service_manage.product.ProductManageService;
import com.project.shoppingmall.testdata.ProductBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

class ProductTypeManageServiceTest {
  private ProductTypeManageService target;
  private ProductTypeService mockProductTypeService;
  private ProductTypeRepository mockProductTypeRepository;
  private ProductManageService mockProductManageService;
  private AlarmService mockAlarmService;
  private EntityManagerService mockEntityManagerService;
  private int givenBatchSize = 3;

  @BeforeEach
  public void beforeEach() {
    mockProductTypeService = mock(ProductTypeService.class);
    mockProductTypeRepository = mock(ProductTypeRepository.class);
    mockProductManageService = mock(ProductManageService.class);
    mockAlarmService = mock(AlarmService.class);
    mockEntityManagerService = mock(EntityManagerService.class);

    target =
        new ProductTypeManageService(
            mockProductTypeService,
            mockProductTypeRepository,
            mockProductManageService,
            mockAlarmService,
            mockEntityManagerService);
    ReflectionTestUtils.setField(target, "batchSize", givenBatchSize);
  }

  @Test
  @DisplayName("save() : 정상흐름")
  public void save_ok() {
    // given
    String inputTypeName = "테스트1$test1";

    // when
    ProductType saveResult = target.save(inputTypeName);

    // then
    ArgumentCaptor<ProductType> productTypeCaptor = ArgumentCaptor.forClass(ProductType.class);
    verify(mockProductTypeRepository, times(1)).save(productTypeCaptor.capture());
    assertEquals(inputTypeName, productTypeCaptor.getValue().getTypeName());
  }

  @Test
  @DisplayName("save() : 적절하지 않은 타입명")
  public void save_incorrectTypeName() {
    // given
    String inputTypeName = "테스트1-test1";

    // when
    assertThrows(ServerLogicError.class, () -> target.save(inputTypeName));
  }

  @Test
  @DisplayName("update() : 정상흐름")
  public void update_ok() throws IOException {
    // given
    long inputTypeId = 10L;
    String inputTypeName = "테스트1$test1";

    set_mockProductTypeService_findById(inputTypeId, "테스트1$given");
    set_mockProductManageService_findProductsByTypeInBatch(1);

    // when
    ProductType updateResult = target.update(inputTypeId, inputTypeName);

    // then
    check_mockProductManageService_findProductsByTypeInBatch();
    check_mockAlarmService_makeAllTypeUpdateAlarm();
    assertEquals(inputTypeName, updateResult.getTypeName());
  }

  @Test
  @DisplayName("update() :  기본 제품타입을 수정하려고 시도")
  public void update_updateProductType() {
    // given
    long inputTypeId = 10L;
    String inputTypeName = "테스트1-test1";

    String baseTypeName = FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME;
    set_mockProductTypeService_findById(inputTypeId, baseTypeName);

    // when
    assertThrows(
        CannotUpdateBaseProductType.class, () -> target.update(inputTypeId, inputTypeName));
  }

  @Test
  @DisplayName("update() : 적절하지 않은 타입명")
  public void update_incorrectTypeName() throws IOException {
    // given
    long inputTypeId = 10L;
    String inputTypeName = "테스트1-test1";

    set_mockProductTypeService_findById(inputTypeId, "테스트1$given");
    set_mockProductManageService_findProductsByTypeInBatch(1);

    // when
    assertThrows(ServerLogicError.class, () -> target.update(inputTypeId, inputTypeName));
  }

  @Test
  @DisplayName("delete() : 정상흐름")
  public void delete_ok() throws IOException {
    // given
    long inputDeletedProductId = 10L;

    long givenBaseProductTypeId = 1L;

    set_mockProductTypeService_findById(inputDeletedProductId, "test$test");
    set_mockProductTypeService_findBaseProductType(givenBaseProductTypeId);
    set_mockProductManageService_findProductsByTypeInBatch(1);

    // when
    target.delete(inputDeletedProductId);

    // then
    check_mockProductManageService_findProductsByTypeInBatch();
    check_mockAlarmService_makeAllTypeDeleteAlarm();
    check_mockProductManageService_changeProductTypeToBaseType(
        givenBaseProductTypeId, inputDeletedProductId);
    check_mockProductTypeRepository_delete(inputDeletedProductId);
  }

  @Test
  @DisplayName("delete() : 기본 제품타입을 삭제하려고 시도")
  public void delete_deleteBaseProductType() {
    // given
    long inputDeletedProductId = 10L;

    String baseTypeName = FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME;

    set_mockProductTypeService_findById(inputDeletedProductId, baseTypeName);
    set_mockProductTypeService_findBaseProductType(inputDeletedProductId);

    // when
    assertThrows(CannotDeleteBaseProductType.class, () -> target.delete(inputDeletedProductId));
  }

  public Slice<Product> makeMockSlice(int contentSize, boolean isFirst, boolean isLast)
      throws IOException {
    List<Product> productList = new ArrayList<>();
    for (int i = 0; i < contentSize; i++) productList.add(ProductBuilder.lightData().build());

    Slice<Product> mockSlice = mock(Slice.class);
    when(mockSlice.getContent()).thenReturn(productList);
    when(mockSlice.hasPrevious()).thenReturn(!isFirst);
    when(mockSlice.hasNext()).thenReturn(!isLast);
    return mockSlice;
  }

  public void set_mockProductManageService_findProductsByTypeInBatch(int lastSliceContentSize)
      throws IOException {
    Slice<Product> firstMock = makeMockSlice(givenBatchSize, true, false);
    Slice<Product> secondMock = makeMockSlice(givenBatchSize, false, false);
    Slice<Product> lastMock = makeMockSlice(lastSliceContentSize, false, true);
    when(mockProductManageService.findProductsByTypeInBatch(anyLong(), anyInt(), anyInt()))
        .thenReturn(firstMock)
        .thenReturn(secondMock)
        .thenReturn(lastMock);
  }

  public void set_mockProductTypeService_findById(long typeId, String typeName) {
    ProductType givenProductType = new ProductType("temp$temp");
    ReflectionTestUtils.setField(givenProductType, "id", typeId);
    ReflectionTestUtils.setField(givenProductType, "typeName", typeName);
    when(mockProductTypeService.findById(anyLong())).thenReturn(Optional.of(givenProductType));
  }

  public void set_mockProductTypeService_findBaseProductType(long baseTypeId) {
    String baseTypeName = FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME;
    ProductType givenProductType = new ProductType("temp$temp");
    ReflectionTestUtils.setField(givenProductType, "id", baseTypeId);
    ReflectionTestUtils.setField(givenProductType, "typeName", baseTypeName);
    when(mockProductTypeService.findBaseProductType()).thenReturn(Optional.of(givenProductType));
  }

  public void check_mockProductManageService_findProductsByTypeInBatch() {
    ArgumentCaptor<Integer> batchNumCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> batchSizeCaptor = ArgumentCaptor.forClass(Integer.class);

    verify(mockProductManageService, times(3))
        .findProductsByTypeInBatch(anyLong(), batchNumCaptor.capture(), batchSizeCaptor.capture());

    List<Integer> expectedBatchNum = List.of(0, 1, 2);
    List<Integer> realBatchNum = batchNumCaptor.getAllValues();
    assertArrayEquals(expectedBatchNum.toArray(), realBatchNum.toArray());

    List<Integer> expectedBatchSize = List.of(givenBatchSize, givenBatchSize, givenBatchSize);
    List<Integer> realBatchSize = batchSizeCaptor.getAllValues();
    assertArrayEquals(expectedBatchSize.toArray(), realBatchSize.toArray());
  }

  public void check_mockAlarmService_makeAllTypeUpdateAlarm() {
    ArgumentCaptor<List<Product>> productListCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockAlarmService, times(3)).makeAllTypeUpdateAlarm(productListCaptor.capture());
    List<Integer> expectedProductListSize = List.of(givenBatchSize, givenBatchSize, 1);
    List<Integer> realProductListSize =
        productListCaptor.getAllValues().stream().map(products -> products.size()).toList();
    assertArrayEquals(expectedProductListSize.toArray(), realProductListSize.toArray());
  }

  public void check_mockAlarmService_makeAllTypeDeleteAlarm() {
    ArgumentCaptor<List<Product>> productListCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockAlarmService, times(3)).makeAllTypeDeleteAlarm(productListCaptor.capture());
    List<Integer> expectedProductListSize = List.of(givenBatchSize, givenBatchSize, 1);
    List<Integer> realProductListSize =
        productListCaptor.getAllValues().stream().map(products -> products.size()).toList();
    assertArrayEquals(expectedProductListSize.toArray(), realProductListSize.toArray());
  }

  public void check_mockProductManageService_changeProductTypeToBaseType(
      long baseTypeId, long deletedProductTypeId) {
    ArgumentCaptor<ProductType> baseProductCaptor = ArgumentCaptor.forClass(ProductType.class);
    ArgumentCaptor<Long> deletedProductTypeIdCaptor = ArgumentCaptor.forClass(Long.class);

    verify(mockProductManageService, times(1))
        .changeProductTypeToBaseType(
            baseProductCaptor.capture(), deletedProductTypeIdCaptor.capture());

    assertEquals(baseTypeId, baseProductCaptor.getValue().getId());
    assertEquals(deletedProductTypeId, deletedProductTypeIdCaptor.getValue());
  }

  public void check_mockProductTypeRepository_delete(long deletedProductTypeId) {
    ArgumentCaptor<ProductType> deletedProductCaptor = ArgumentCaptor.forClass(ProductType.class);
    verify(mockProductTypeRepository, times(1)).delete(deletedProductCaptor.capture());
    assertSame(deletedProductTypeId, deletedProductCaptor.getValue().getId());
  }
}
