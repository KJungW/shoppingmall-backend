package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.type.ProductSaleType;
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

  private Member givenSeller;
  private ProductType givenType;

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
    givenSeller = seller;

    // 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);
    givenType = type;

    // 제품 15개 생성 (벤처리x, 판매중)
    for (int i = 0; i < 15; i++) {
      Product targetProduct =
          Product.builder()
              .seller(seller)
              .productType(type)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("정상 유형의 판매상품")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }

    // 제품 5개 생성 (벤처리x, 판매중단)
    for (int i = 0; i < 5; i++) {
      Product targetProduct =
          Product.builder()
              .seller(seller)
              .productType(type)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("판매중단된 판매상품")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      targetProduct.changeSalesStateToDiscontinued();
      em.persist(targetProduct);
    }

    // 제품 5개 생성 (벤처리o, 판매중)
    for (int i = 0; i < 5; i++) {
      Product targetProduct =
          Product.builder()
              .seller(seller)
              .productType(type)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("벤처리된 판매상품")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(true)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
  }

  @Test
  @DisplayName("findByProductType() : 정상흐름 - 첫번째 페이지")
  public void findByProductType_ok_firstPage() {
    // given
    // - 별도 타입의 제품 8개 생성
    ProductType givenNewType = new ProductType("new$productType");
    em.persist(givenNewType);
    for (int i = 0; i < 8; i++) {
      Product targetProduct =
          Product.builder()
              .seller(givenSeller)
              .productType(givenNewType)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("정상 유형의 판매상품")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
    // - 인자 생성
    long givenTypeId = givenNewType.getId();
    PageRequest givenPageRequest = PageRequest.of(0, 5);

    // when
    Slice<Product> sliceResult = target.findByProductType(givenTypeId, givenPageRequest);

    // then
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    assertEquals(5, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
  }

  @Test
  @DisplayName("findByProductType() : 정상흐름 - 마지막 페이지")
  public void findByProductType_ok_lastPage() {
    // given
    // - 별도 타입의 제품 8개 생성
    ProductType givenNewType = new ProductType("new$productType");
    em.persist(givenNewType);
    for (int i = 0; i < 8; i++) {
      Product targetProduct =
          Product.builder()
              .seller(givenSeller)
              .productType(givenNewType)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("정상 유형의 판매상품")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
    // - 인자 생성
    long givenTypeId = givenNewType.getId();
    PageRequest givenPageRequest = PageRequest.of(1, 5);

    // when
    Slice<Product> sliceResult = target.findByProductType(givenTypeId, givenPageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    assertEquals(3, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
  }

  @Test
  @DisplayName("findBySearchWord() : 정상흐름 - 첫번째 페이지")
  public void findBySearchWord_ok_firstPage() {
    // given
    // - 별도 타입의 제품 8개 생성
    for (int i = 0; i < 8; i++) {
      Product targetProduct =
          Product.builder()
              .seller(givenSeller)
              .productType(givenType)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("검색 기능 관련 테스트")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
    // - 인자 생성
    String searchWord = "검색";
    PageRequest givenPageRequest = PageRequest.of(0, 5);

    // when
    Slice<Product> sliceResult = target.findBySearchWord(searchWord, givenPageRequest);

    // then
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    assertEquals(5, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertTrue(product.getName().contains("검색"));
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
  }

  @Test
  @DisplayName("findBySearchWord() : 정상흐름 - 마지막 페이지")
  public void findBySearchWord_ok_lastPage() {
    // given
    // - 별도 타입의 제품 8개 생성
    for (int i = 0; i < 8; i++) {
      Product targetProduct =
          Product.builder()
              .seller(givenSeller)
              .productType(givenType)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("검색 기능 관련 테스트")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
    // - 인자 생성
    String searchWord = "검색";
    PageRequest givenPageRequest = PageRequest.of(1, 5);

    // when
    Slice<Product> sliceResult = target.findBySearchWord(searchWord, givenPageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    assertEquals(3, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertTrue(product.getName().contains("검색"));
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
  }

  @Test
  @DisplayName("findAllBySeller() : 정상흐름 - 첫번째 페이지")
  public void findAllBySeller_ok_firstPage() {
    // given
    // - 별도의 판매자 생성
    Member givenNewMember =
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
    em.persist(givenNewMember);
    // - 생성한 판매자에 대해 판매상품 8개 생성
    for (int i = 0; i < 8; i++) {
      Product targetProduct =
          Product.builder()
              .seller(givenNewMember)
              .productType(givenType)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("검색 기능 관련 테스트")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
    long givenSellerId = givenNewMember.getId();
    PageRequest givenPageRequest = PageRequest.of(0, 5);

    // when
    Slice<Product> sliceResult = target.findAllBySeller(givenSellerId, givenPageRequest);

    // then
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    assertEquals(5, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertEquals(givenSellerId, product.getSeller().getId());
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
  }

  @Test
  @DisplayName("findAllBySeller() : 정상흐름 - 첫번째 페이지")
  public void findAllBySeller_ok_lastPage() {
    // given
    // - 별도의 판매자 생성
    Member givenNewMember =
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
    em.persist(givenNewMember);
    // - 생성한 판매자에 대해 판매상품 8개 생성
    for (int i = 0; i < 8; i++) {
      Product targetProduct =
          Product.builder()
              .seller(givenNewMember)
              .productType(givenType)
              .productImages(new ArrayList<>())
              .contents(new ArrayList<>())
              .singleOptions(new ArrayList<>())
              .multipleOptions(new ArrayList<>())
              .name("검색 기능 관련 테스트")
              .price(2000)
              .discountAmount(100)
              .discountRate(10.0)
              .isBan(false)
              .scoreAvg(0.0)
              .build();
      em.persist(targetProduct);
    }
    long givenSellerId = givenNewMember.getId();
    PageRequest givenPageRequest = PageRequest.of(1, 5);

    // when
    Slice<Product> sliceResult = target.findAllBySeller(givenSellerId, givenPageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    assertEquals(3, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertEquals(givenSellerId, product.getSeller().getId());
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
  }

  @Test
  @DisplayName("findAllByRandom() : 정상흐름 - 첫번째 페이지")
  public void findAllByRandom_ok_firstPage() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    Slice<Product> sliceResult = target.findAllByRandom(pageRequest);

    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    assertEquals(10, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
    HashSet<Long> productHashSet =
        new HashSet<>(sliceResult.getContent().stream().map(Product::getId).toList());
    assertEquals(sliceResult.getContent().size(), productHashSet.size());
  }

  @Test
  @DisplayName("findAllByRandom() : 정상흐름 - 마지막 페이지")
  public void findAllByRandom_ok_lastPage() {
    PageRequest pageRequest = PageRequest.of(1, 10);

    Slice<Product> sliceResult = target.findAllByRandom(pageRequest);

    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    assertEquals(5, sliceResult.getContent().size());
    sliceResult
        .getContent()
        .forEach(
            product -> {
              assertFalse(product.getIsBan());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
            });
    HashSet<Long> productHashSet =
        new HashSet<>(sliceResult.getContent().stream().map(Product::getId).toList());
    assertEquals(sliceResult.getContent().size(), productHashSet.size());
  }
}
