package com.project.shoppingmall.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "WRITER_ID")
  private Member writer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  private String reviewImageUrl;
  private Integer score;
  private String title;
  private String description;
  private Boolean isBan;
}
