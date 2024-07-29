package com.project.shoppingmall.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductType extends BaseEntity {
  @Id @GeneratedValue private Long id;
  private String typeName;

  public ProductType(String typeName) {
    this.typeName = typeName;
  }
}
