package com.project.shoppingmall.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.CannotSaveProductBecauseMemberBan;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.MemberAccountIsNotRegistered;
import com.project.shoppingmall.exception.WrongPriceAndDiscount;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.product.ProductChecker;
import com.project.shoppingmall.test_entity.product.ProductMakeDataBuilder;
import com.project.shoppingmall.test_entity.product_type.ProductTypeBuilder;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.ProductSaleType;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

class ProductServiceTest {
  private ProductService productService;
  private MemberFindService mockMemberFindService;
  private ProductTypeService productTypeService;
  private ProductRepository productRepository;
  private ProductFindService productFindService;
  private S3Service s3Service;

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    productTypeService = mock(ProductTypeService.class);
    productRepository = mock(ProductRepository.class);
    productFindService = mock(ProductFindService.class);
    s3Service = mock(S3Service.class);
    productService =
        new ProductService(
            mockMemberFindService,
            productTypeService,
            productRepository,
            productFindService,
            s3Service);
  }

  @Test
  @DisplayName("save() : 정상흐름")
  public void save_ok() {
    // given
    Long inputMemberId = 1L;
    ProductMakeData inputMakeData = ProductMakeDataBuilder.fullData().build();

    Member givenMember =
        MemberBuilder.makeMemberWithAccountNumber(
            inputMemberId, LoginType.NAVER, "123124-512412-123");
    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputMakeData.getProductTypeId(), "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    Product savedProduct = productService.save(inputMemberId, inputMakeData);

    // then
    check_s3Service_uploadFile(inputMakeData);
    ProductChecker.checkProduct(givenMember, inputMakeData, givenFileUploadResult, savedProduct);
  }

  @Test
  @DisplayName("save() : 잘못된 가격과 할인")
  public void save_wrongPriceAndDiscount() {
    // given
    Long inputMemberId = 1L;
    ProductMakeData inputMakeData =
        ProductMakeDataBuilder.fullData()
            .price(10000)
            .discountAmount(5000)
            .discountRate(50d)
            .build();

    Member givenMember =
        MemberBuilder.makeMemberWithAccountNumber(
            inputMemberId, LoginType.NAVER, "123124-512412-123");
    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputMakeData.getProductTypeId(), "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    assertThrows(
        WrongPriceAndDiscount.class, () -> productService.save(inputMemberId, inputMakeData));
  }

  @Test
  @DisplayName("save() : 벤상태의 회원이 제품을 등록하려 시도함")
  public void save_bannedMember() {
    // given
    Long inputMemberId = 1L;
    ProductMakeData inputMakeData = ProductMakeDataBuilder.fullData().build();

    Member givenMember =
        MemberBuilder.makeMemberWithAccountNumber(
            inputMemberId, LoginType.NAVER, "123124-512412-123");
    ReflectionTestUtils.setField(givenMember, "isBan", true);
    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputMakeData.getProductTypeId(), "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    assertThrows(
        CannotSaveProductBecauseMemberBan.class,
        () -> productService.save(inputMemberId, inputMakeData));
  }

  @Test
  @DisplayName("save() : 계좌를 등록하지 않은 회원이 제품을 등록하려고 시도")
  public void save_notRegisterAccount() {
    // given
    Long inputMemberId = 1L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId, LoginType.NAVER);
    ProductType givenProductType = ProductTypeBuilder.makeProductType(2L, "test$detail");
    FileUploadResult givenFileUploadResult = new FileUploadResult("severuri/test", "download/test");
    ProductMakeData inputMakeData = ProductMakeDataBuilder.makeProduct(givenProductType);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUploadResult);

    // when
    assertThrows(
        MemberAccountIsNotRegistered.class,
        () -> productService.save(inputMemberId, inputMakeData));
  }

  @Test
  @DisplayName("update() : 정상흐름")
  public void update_ok() {
    // given
    Long inputMemberId = 10L;
    Long inputProductId = 13L;
    ProductMakeData inputMakeData = ProductMakeDataBuilder.fullData().build();

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputMakeData.getProductTypeId(), "test$detail");
    Product givenProduct =
        ProductBuilder.makeProduct(inputProductId, givenSeller, givenProductType);
    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");

    when(productFindService.findById(any())).thenReturn(Optional.of(givenProduct));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    // when
    Product result = productService.update(inputMemberId, inputProductId, inputMakeData);

    // then
    check_s3Service_uploadFile(inputMakeData);
    check_s3Service_deleteFile(givenProduct);
    ProductChecker.checkProduct(givenSeller, inputMakeData, givenFileUpload, result);
  }

  @Test
  @DisplayName("update() : 다른 회원의 제품을 수정하려고 시도")
  public void update_otherMemberProduct() {
    // given
    Long inputMemberId = 10L;
    Long inputProductId = 13L;
    ProductMakeData inputMakeData = ProductMakeDataBuilder.fullData().build();

    Member givenOtherMember = MemberBuilder.makeMember(60234L);
    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputMakeData.getProductTypeId(), "test$detail");
    Product givenProduct =
        ProductBuilder.makeProduct(inputProductId, givenOtherMember, givenProductType);
    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");

    when(productFindService.findById(any())).thenReturn(Optional.of(givenProduct));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    // when
    assertThrows(
        DataNotFound.class,
        () -> productService.update(inputMemberId, inputProductId, inputMakeData));

    // then
    verify(s3Service, times(0)).deleteFile(any());
    verify(s3Service, times(0)).uploadFile(any(), any());
  }

  @Test
  @DisplayName("update() : 잘못된 가격과 할인으로 수정을 시도")
  public void update_wrongPriceAndDiscount() {
    // given
    Long inputMemberId = 10L;
    Long inputProductId = 13L;
    ProductMakeData inputMakeData =
        ProductMakeDataBuilder.fullData()
            .price(10000)
            .discountAmount(5000)
            .discountRate(50d)
            .build();

    Member givenSeller = MemberBuilder.makeMember(inputMemberId);
    ProductType givenProductType =
        ProductTypeBuilder.makeProductType(inputMakeData.getProductTypeId(), "test$detail");
    Product givenProduct =
        ProductBuilder.makeProduct(inputProductId, givenSeller, givenProductType);
    FileUploadResult givenFileUpload = new FileUploadResult("severuri/test", "download/test");

    when(productFindService.findById(any())).thenReturn(Optional.of(givenProduct));
    when(productTypeService.findById(any())).thenReturn(Optional.of(givenProductType));
    when(s3Service.uploadFile(any(), any())).thenReturn(givenFileUpload);

    // when
    assertThrows(
        WrongPriceAndDiscount.class,
        () -> productService.update(inputMemberId, inputProductId, inputMakeData));

    // then
    verify(s3Service, times(0)).deleteFile(any());
    verify(s3Service, times(0)).uploadFile(any(), any());
  }

  @Test
  @DisplayName("changeProductToOnSale() : 정상흐름")
  public void changeProductToOnSale_ok() {
    // given
    long givenMemberId = 10L;
    long givenProductId = 32L;

    Member givenSeller = MemberBuilder.makeMember(givenMemberId);
    Product givenProduct = ProductBuilder.makeProduct(givenProductId, givenSeller);
    ReflectionTestUtils.setField(givenProduct, "saleState", ProductSaleType.DISCONTINUED);

    when(productFindService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    Product product = productService.changeProductToOnSale(givenMemberId, givenProductId);

    // then
    assertEquals(givenProductId, product.getId());
    assertEquals(givenMemberId, product.getSeller().getId());
    assertEquals(ProductSaleType.ON_SALE, givenProduct.getSaleState());
  }

  @Test
  @DisplayName("changeProductToDiscontinued() : 정상흐름")
  public void changeProductToDiscontinued_ok() {
    // given
    long givenMemberId = 10L;
    long givenProductId = 32L;

    Member givenSeller = MemberBuilder.makeMember(givenMemberId);
    Product givenProduct = ProductBuilder.makeProduct(givenProductId, givenSeller);
    ReflectionTestUtils.setField(givenProduct, "saleState", ProductSaleType.ON_SALE);

    when(productFindService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    Product product = productService.changeProductToDiscontinued(givenMemberId, givenProductId);

    // then
    assertEquals(givenProductId, product.getId());
    assertEquals(givenMemberId, product.getSeller().getId());
    assertEquals(ProductSaleType.DISCONTINUED, givenProduct.getSaleState());
  }

  private void check_s3Service_uploadFile(ProductMakeData givenMakeData) {
    int givenImageBlockCount =
        givenMakeData.getContentBlocks().stream()
            .filter(block -> block.getBlockType().equals(BlockType.IMAGE_TYPE))
            .toList()
            .size();
    int expectedTimes = givenMakeData.getProductImages().size() + givenImageBlockCount;
    verify(s3Service, times(expectedTimes)).uploadFile(any(), any());
  }

  private void check_s3Service_deleteFile(Product givenProduct) {
    int givenImageBlockCount =
        givenProduct.getContents().stream()
            .filter(content -> content.getType().equals(BlockType.IMAGE_TYPE))
            .toList()
            .size();
    int givenProductImageCount = givenProduct.getProductImages().size();
    int expectedTimes = givenImageBlockCount + givenProductImageCount;
    verify(s3Service, times(expectedTimes)).deleteFile(any());
  }
}
