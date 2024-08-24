package com.project.shoppingmall.service_manage.prodcut_type;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.exception.CannotDeleteBaseProductType;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.final_value.FinalValue;
import com.project.shoppingmall.repository.ProductTypeRepository;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import com.project.shoppingmall.service_manage.product.ProductManageService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ProductTypeManageServiceTest {
  private ProductTypeManageService target;
  private ProductTypeService mockProductTypeService;
  private ProductTypeRepository mockProductTypeRepository;
  private ProductManageService mockProductManageService;

  @BeforeEach
  public void beforeEach() {
    this.mockProductTypeService = mock(ProductTypeService.class);
    this.mockProductTypeRepository = mock(ProductTypeRepository.class);
    this.mockProductManageService = mock(ProductManageService.class);
    this.target =
        new ProductTypeManageService(
            mockProductTypeService, mockProductTypeRepository, mockProductManageService);
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
  public void update_ok() {
    // given
    long inputTypeId = 10L;
    String inputTypeName = "테스트1$test1";

    ProductType givenProductType = new ProductType("테스트1$given");
    ReflectionTestUtils.setField(givenProductType, "id", inputTypeId);
    when(mockProductTypeService.findById(anyLong())).thenReturn(Optional.of(givenProductType));

    // when
    ProductType updateResult = target.update(inputTypeId, inputTypeName);

    // then
    assertEquals(inputTypeName, updateResult.getTypeName());
  }

  @Test
  @DisplayName("update() : 적절하지 않은 타입명")
  public void update_incorrectTypeName() {
    // given
    long inputTypeId = 10L;
    String inputTypeName = "테스트1-test1";

    ProductType givenProductType = new ProductType("테스트1$given");
    ReflectionTestUtils.setField(givenProductType, "id", inputTypeId);
    when(mockProductTypeService.findById(anyLong())).thenReturn(Optional.of(givenProductType));

    // when
    assertThrows(ServerLogicError.class, () -> target.update(inputTypeId, inputTypeName));
  }

  @Test
  @DisplayName("delete() : 정상흐름")
  public void delete_ok() {
    // given
    long inputProductTypeId = 10L;

    ProductType givenDeletedProductType = new ProductType("test$test");
    ReflectionTestUtils.setField(givenDeletedProductType, "id", inputProductTypeId);
    when(mockProductTypeService.findById(anyLong()))
        .thenReturn(Optional.of(givenDeletedProductType));

    long givenBaseProductTypeId = 1L;
    ProductType givenBaseProductType = new ProductType("temp$temp");
    ReflectionTestUtils.setField(givenBaseProductType, "id", givenBaseProductTypeId);
    ReflectionTestUtils.setField(
        givenBaseProductType,
        "typeName",
        FinalValue.BASE_PRODUCT_TYPE_PREFIX + "$" + FinalValue.BASE_PRODUCT_TYPE_NAME);
    when(mockProductTypeService.findBaseProductType())
        .thenReturn(Optional.of(givenBaseProductType));

    // when
    target.delete(inputProductTypeId);

    // then
    ArgumentCaptor<ProductType> baseProductCaptor = ArgumentCaptor.forClass(ProductType.class);
    ArgumentCaptor<Long> deletedProductTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockProductManageService, times(1))
        .changeProductTypeToBaseType(
            baseProductCaptor.capture(), deletedProductTypeIdCaptor.capture());
    assertSame(givenBaseProductType, baseProductCaptor.getValue());
    assertEquals(inputProductTypeId, deletedProductTypeIdCaptor.getValue());

    ArgumentCaptor<ProductType> deletedProductCaptor = ArgumentCaptor.forClass(ProductType.class);
    verify(mockProductTypeRepository, times(1)).delete(deletedProductCaptor.capture());
    assertSame(givenDeletedProductType, deletedProductCaptor.getValue());
  }

  @Test
  @DisplayName("delete() : 기본 제품타입을 삭제하려고 시도")
  public void delete_deleteBaseProductType() {
    // given
    long inputProductTypeId = 10L;

    ProductType givenBaseProductType = new ProductType("temp$temp");
    ReflectionTestUtils.setField(givenBaseProductType, "id", inputProductTypeId);
    ReflectionTestUtils.setField(
        givenBaseProductType,
        "typeName",
        FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME);
    when(mockProductTypeService.findById(anyLong())).thenReturn(Optional.of(givenBaseProductType));
    when(mockProductTypeService.findBaseProductType())
        .thenReturn(Optional.of(givenBaseProductType));

    // when
    assertThrows(CannotDeleteBaseProductType.class, () -> target.delete(inputProductTypeId));
  }
}
