package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ProductRetrieveRepositoryTest {
  @Autowired private ProductRetrieveRepository target;
  @Autowired private EntityManager em;
  private Long givenBuyerId;
  private Long givenSellerId;

  @BeforeEach
  public void beforeEach() {
    // 판매자와 구매자 생성
    Member seller =
        Member.builder()
            .loginType(LoginType.NAVER)
            .socialId("testMemberSocialId123123421aaa")
            .nickName("seller")
            .profileImageUrl("init/member_profile_img.png")
            .profileImageDownLoadUrl(
                "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/member_profile_img.png")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    em.persist(seller);
    givenSellerId = seller.getId();

    // 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);

    // 제품 15개 생성
    for (int i = 0; i < 15; i++) {
      Product targetProduct =
          Product.builder()
              .seller(seller)
              .productType(type)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("test입니다.")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
  }

  @Test
  @DisplayName("findAllByRandom() : 정상흐름 - 첫번째 페이지")
  public void findAllByRandom_ok_firstPage() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    Slice<Product> sliceResult = target.findAllByRandom(pageRequest);

    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    assertEquals(10, sliceResult.getContent().size());
    HashSet<Long> productHashSet =
        new HashSet<>(sliceResult.getContent().stream().map(Product::getId).toList());
    assertEquals(10, productHashSet.size());
  }

  @Test
  @DisplayName("findAllByRandom() : 정상흐름 - 마지막 페이지")
  public void findAllByRandom_ok_lastPage() {
    PageRequest pageRequest = PageRequest.of(1, 10);

    Slice<Product> sliceResult = target.findAllByRandom(pageRequest);

    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    assertEquals(5, sliceResult.getContent().size());
    HashSet<Long> productHashSet =
        new HashSet<>(sliceResult.getContent().stream().map(Product::getId).toList());
    assertEquals(5, productHashSet.size());
  }
}
