package com.project.shoppingmall.init;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.repository.MemberRepository;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.repository.ProductTypeRepository;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class InitDbData {
  @Value("${spring.config.activate.on-profile}")
  private String envType;

  @Value("${spring.jpa.hibernate.ddl-auto}")
  private String ddlType;

  private final ProductTypeRepository productTypeRepository;
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;

  @PostConstruct
  public void init() {
    if (envType.equals("dev") || envType.equals("stage")) {
      if (ddlType.equals("create") || ddlType.equals("create-drop")) {
        initData();
      }
    }
  }

  private void initData() {
    Random randomGenerator = new Random();

    // 제품 타입 생성
    ProductType type1 = productTypeRepository.save(new ProductType("게임$타이틀"));
    ProductType type2 = productTypeRepository.save(new ProductType("게임$게임기"));
    ProductType type3 = productTypeRepository.save(new ProductType("도서$소설"));
    ProductType type4 = productTypeRepository.save(new ProductType("도서$만화책"));
    ProductType type5 = productTypeRepository.save(new ProductType("식표품$과일"));
    ProductType type6 = productTypeRepository.save(new ProductType("식표품$밀키트"));
    List<ProductType> typeList =
        new ArrayList<>(Arrays.asList(type1, type2, type3, type4, type5, type6));

    // 회원 생성
    Member member =
        Member.builder()
            .loginType(LoginType.NAVER)
            .socialId("testMemberSocialId123123421aaa")
            .nickName("Kim")
            .profileImageUrl("init/member_profile_img.png")
            .profileImageDownLoadUrl(
                "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/member_profile_img.png")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    memberRepository.save(member);

    // 타입별로 30개의 제품 생성
    for (ProductType type : typeList) {
      String productName = type.getTypeName().replace("$", "-");
      for (int i = 0; i < 30; i++) {
        Product product =
            Product.builder()
                .seller(member)
                .productType(type)
                .productImages(new ArrayList<>())
                .contents(new ArrayList<>())
                .singleOptions(new ArrayList<>())
                .multipleOptions(new ArrayList<>())
                .name(productName + "-임시제품-" + i)
                .price((randomGenerator.nextInt(2) + 1) * 100)
                .discountAmount(randomGenerator.nextInt(6))
                .discountRate(randomGenerator.nextInt(2) * 10.0)
                .isBan(false)
                .scoreAvg((double) randomGenerator.nextInt(6))
                .build();

        ProductImage productImg1 =
            ProductImage.builder()
                .imageUri("init/product_img1.jpg")
                .downLoadUrl(
                    "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_img1.jpg")
                .build();
        ProductImage productImg2 =
            ProductImage.builder()
                .imageUri("init/product_img2.jpg")
                .downLoadUrl(
                    "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_img2.jpg")
                .build();
        ProductImage productImg3 =
            ProductImage.builder()
                .imageUri("init/product_img3.jpg")
                .downLoadUrl(
                    "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_img3.jpg")
                .build();
        List<ProductImage> productImages =
            new ArrayList<>(Arrays.asList(productImg1, productImg2, productImg3));
        product.updateProductImages(productImages);

        ProductContent content1 =
            ProductContent.builder()
                .type(BlockType.TEXT_TYPE)
                .content(JsonUtil.convertObjectToJson(new TextBlock(1L, "임시적인 텍스트블록입니다")))
                .build();
        ProductContent content2 =
            ProductContent.builder()
                .type(BlockType.IMAGE_TYPE)
                .content(
                    JsonUtil.convertObjectToJson(
                        new ImageBlock(
                            2L,
                            "init/product_content_img1.jpg",
                            "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_content_img1.jpg")))
                .build();
        ProductContent content3 =
            ProductContent.builder()
                .type(BlockType.IMAGE_TYPE)
                .content(
                    JsonUtil.convertObjectToJson(
                        new ImageBlock(
                            3L,
                            "init/product_content_img2.jpg",
                            "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_content_img2.jpg")))
                .build();
        ProductContent content4 =
            ProductContent.builder()
                .type(BlockType.IMAGE_TYPE)
                .content(
                    JsonUtil.convertObjectToJson(
                        new ImageBlock(
                            4L,
                            "init/product_content_img3.jpg",
                            "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/product_content_img3.jpg")))
                .build();
        List<ProductContent> productContents =
            new ArrayList<>(Arrays.asList(content1, content2, content3, content4));
        product.updateContents(productContents);

        ProductSingleOption singleOption1 =
            ProductSingleOption.builder().optionName("임시 단일옵션1").priceChangeAmount(5).build();
        ProductSingleOption singleOption2 =
            ProductSingleOption.builder().optionName("임시 단일옵션2").priceChangeAmount(5).build();
        ProductSingleOption singleOption3 =
            ProductSingleOption.builder().optionName("임시 단일옵션3").priceChangeAmount(5).build();
        List<ProductSingleOption> singleOptions =
            new ArrayList<>(Arrays.asList(singleOption1, singleOption2, singleOption3));
        product.updateSingleOption(singleOptions);

        ProductMultipleOption multiOption1 =
            ProductMultipleOption.builder().optionName("임시 다중옵션1").priceChangeAmount(5).build();
        ProductMultipleOption multiOption2 =
            ProductMultipleOption.builder().optionName("임시 다중옵션2").priceChangeAmount(5).build();
        ProductMultipleOption multiOption3 =
            ProductMultipleOption.builder().optionName("임시 다중옵션3").priceChangeAmount(5).build();
        List<ProductMultipleOption> multiOptions =
            new ArrayList<>(Arrays.asList(multiOption1, multiOption2, multiOption3));
        product.updateMultiOptions(multiOptions);

        productRepository.save(product);
      }
    }
  }
}
