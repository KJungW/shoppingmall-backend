package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.final_value.RegularExpressions;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductType extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String typeName;

  public ProductType(String typeName) {
    updateTypeName(typeName);
  }

  public void updateTypeName(String typeName) {
    if (typeName == null || typeName.isBlank())
      throw new ServerLogicError("ProductType의 typeName필드에 빈값이 입력되었습니다.");
    if (!Pattern.matches(RegularExpressions.PRODUCT_TYPE_PATTERN, typeName))
      throw new ServerLogicError("ProductType의 typeName필드에 부적절한 값이 입력되었습니다.");
    this.typeName = typeName;
  }
}
