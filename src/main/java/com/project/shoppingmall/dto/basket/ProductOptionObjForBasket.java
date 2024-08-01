package com.project.shoppingmall.dto.basket;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductOptionObjForBasket {
  private Long singleOptionId;
  private List<Long> multipleOptionId = new ArrayList<>();
}
