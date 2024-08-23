package com.project.shoppingmall.service.product_type;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ProductTypeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ProductTypeServiceTest {
  private ProductTypeService target;
  private ProductTypeRepository mockProductTypeRepository;

  @BeforeEach
  public void beforeEach() {
    this.mockProductTypeRepository = mock(ProductTypeRepository.class);
    this.target = new ProductTypeService(mockProductTypeRepository);
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
    when(mockProductTypeRepository.findById(anyLong())).thenReturn(Optional.of(givenProductType));

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
    when(mockProductTypeRepository.findById(anyLong())).thenReturn(Optional.of(givenProductType));

    // when
    assertThrows(ServerLogicError.class, () -> target.update(inputTypeId, inputTypeName));
  }
}
