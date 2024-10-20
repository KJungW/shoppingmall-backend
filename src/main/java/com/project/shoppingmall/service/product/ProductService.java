package com.project.shoppingmall.service.product;

import com.project.shoppingmall.dto.block.ContentBlock;
import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.ImageBlockBeforeImageSave;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.dto.product.ProductOption;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.CannotSaveProductBecauseMemberBan;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.InvalidEnumType;
import com.project.shoppingmall.exception.MemberAccountIsNotRegistered;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product_type.ProductTypeService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
  private final MemberFindService memberFindService;
  private final ProductTypeService productTypeService;
  private final ProductRepository productRepository;
  private final ProductFindService productFindService;
  private final S3Service s3Service;

  @Transactional
  public Product save(Long memberId, ProductMakeData productData) {
    Member seller =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    ProductType productType =
        productTypeService
            .findById(productData.getProductTypeId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 제품타입이 존재하지 않습니다."));

    checkPriceAvailable(productData);
    checkSellerIsBan(seller);
    checkMemberAccountIsAvailable(seller);

    List<ProductSingleOption> productSingleOptions =
        makeProductSingleOptionList(productData.getSingleOptions());
    List<ProductMultipleOption> productMultipleOptions =
        makeProductMultipleOptionList(productData.getMultiOptions());
    ArrayList<ProductImage> productImages =
        makeProductImageList(productData.getProductImages(), "productImage/" + seller.getId());
    ArrayList<ProductContent> productContents =
        makeProductContentsList(productData.getContentBlocks(), "blockImage/" + seller.getId());

    Product newProduct =
        Product.builder()
            .seller(seller)
            .productType(productType)
            .name(productData.getName())
            .price(productData.getPrice())
            .discountAmount(productData.getDiscountAmount())
            .discountRate(productData.getDiscountRate())
            .isBan(false)
            .scoreAvg(0.0)
            .singleOptions(productSingleOptions)
            .multipleOptions(productMultipleOptions)
            .productImages(productImages)
            .contents(productContents)
            .build();

    productRepository.save(newProduct);
    return newProduct;
  }

  @Transactional
  public Product update(Long memberId, Long productId, ProductMakeData productData) {
    Product product =
        productFindService
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    ProductType productType =
        productTypeService
            .findById(productData.getProductTypeId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 제품타입이 존재하지 않습니다."));

    checkMemberIsProductSeller(product, memberId);
    checkPriceAvailable(productData);

    removeOriginProductImages(product);
    removeOriginProductContentImage(product);

    ArrayList<ProductImage> productImages =
        makeProductImageList(
            productData.getProductImages(), "productImage/" + product.getSeller().getId());
    ArrayList<ProductContent> productContents =
        makeProductContentsList(
            productData.getContentBlocks(), "blockImage/" + product.getSeller().getId());
    List<ProductSingleOption> productSingleOption =
        makeProductSingleOptionList(productData.getSingleOptions());
    List<ProductMultipleOption> productMultipleOptions =
        makeProductMultipleOptionList(productData.getMultiOptions());

    product.changeProductType(productType);
    product.changeProductName(productData.getName());
    product.changePrice(
        productData.getPrice(), productData.getDiscountAmount(), productData.getDiscountRate());
    product.updateSingleOption(productSingleOption);
    product.updateMultiOptions(productMultipleOptions);
    product.updateProductImages(productImages);
    product.updateContents(productContents);

    return product;
  }

  @Transactional
  public Product changeProductToOnSale(long memberId, long productId) {
    Product product =
        productFindService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));
    if (!product.getSeller().getId().equals(memberId)) {
      throw new DataNotFound("다른 회원이 판매하는 제품의 판매상태를 변경하려고 시도하고있습니다.");
    }
    product.changeSalesStateToOnSale();
    return product;
  }

  @Transactional
  public Product changeProductToDiscontinued(long memberId, long productId) {
    Product product =
        productFindService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 제품이 존재하지 않습니다."));
    if (!product.getSeller().getId().equals(memberId)) {
      throw new DataNotFound("다른 회원이 판매하는 제품의 판매상태를 변경하려고 시도하고있습니다.");
    }
    product.changeSalesStateToDiscontinued();
    return product;
  }

  private void removeOriginProductImages(Product product) {
    List<ProductImage> originProductImage = product.getProductImages();
    for (ProductImage productImage : originProductImage) {
      s3Service.deleteFile(productImage.getImageUri());
    }
  }

  private void removeOriginProductContentImage(Product product) {
    List<ProductContent> originProductContents = product.getContents();
    for (ProductContent productContent : originProductContents) {
      if (productContent.getType().equals(BlockType.IMAGE_TYPE)) {
        ImageBlock imageBlock =
            JsonUtil.convertJsonToObject(productContent.getContent(), ImageBlock.class);
        s3Service.deleteFile(imageBlock.getImageUri());
      }
    }
  }

  private ArrayList<ProductImage> makeProductImageList(
      List<MultipartFile> images, String imageUploadUri) {
    ArrayList<ProductImage> productImagesList = new ArrayList<>();
    for (MultipartFile productImage : images) {
      FileUploadResult uploadResult = s3Service.uploadFile(productImage, imageUploadUri);
      ProductImage newProductImage =
          ProductImage.builder()
              .imageUri(uploadResult.getFileServerUri())
              .downLoadUrl(uploadResult.getDownLoadUrl())
              .build();
      productImagesList.add(newProductImage);
    }
    return productImagesList;
  }

  private ArrayList<ProductContent> makeProductContentsList(
      List<ContentBlock> blocks, String imageUploadUri) {
    ArrayList<ProductContent> productContents = new ArrayList<>();
    for (ContentBlock block : blocks) {
      if (block.getBlockType().equals(BlockType.TEXT_TYPE)) {
        ProductContent textContent = makeProductTextContent((TextBlock) block);
        productContents.add(textContent);
      } else if (block.getBlockType().equals(BlockType.IMAGE_TYPE)) {
        ProductContent imageContent =
            makeProductImageContent((ImageBlockBeforeImageSave) block, imageUploadUri);
        productContents.add(imageContent);
      } else {
        throw new InvalidEnumType("블록 타입이 유효하지 않습니다.");
      }
    }
    return productContents;
  }

  private ProductContent makeProductTextContent(TextBlock textBlock) {
    return ProductContent.builder()
        .type(BlockType.TEXT_TYPE)
        .content(JsonUtil.convertObjectToJson(textBlock))
        .build();
  }

  private ProductContent makeProductImageContent(
      ImageBlockBeforeImageSave imageBlockBefore, String imageUploadUri) {
    FileUploadResult uploadResult =
        s3Service.uploadFile(imageBlockBefore.getImage(), imageUploadUri);
    ImageBlock imageBlock =
        new ImageBlock(
            imageBlockBefore.getIndex(),
            uploadResult.getFileServerUri(),
            uploadResult.getDownLoadUrl());
    return ProductContent.builder()
        .type(BlockType.IMAGE_TYPE)
        .content(JsonUtil.convertObjectToJson(imageBlock))
        .build();
  }

  private List<ProductSingleOption> makeProductSingleOptionList(List<ProductOption> options) {
    List<ProductSingleOption> productSingleOptions = new ArrayList<>();
    for (ProductOption optionData : options) {
      ProductSingleOption newProductSingleOption =
          ProductSingleOption.builder()
              .optionName(optionData.getOptionName())
              .priceChangeAmount(optionData.getPriceChangeAmount())
              .build();
      productSingleOptions.add(newProductSingleOption);
    }
    return productSingleOptions;
  }

  private List<ProductMultipleOption> makeProductMultipleOptionList(List<ProductOption> options) {
    List<ProductMultipleOption> productMultipleOptions = new ArrayList<>();
    for (ProductOption optionData : options) {
      ProductMultipleOption newProductMultipleOption =
          ProductMultipleOption.builder()
              .optionName(optionData.getOptionName())
              .priceChangeAmount(optionData.getPriceChangeAmount())
              .build();
      productMultipleOptions.add(newProductMultipleOption);
    }
    return productMultipleOptions;
  }

  private void checkPriceAvailable(ProductMakeData productData) {
    PriceCalculateUtil.calculatePrice(
        productData.getPrice(), productData.getDiscountAmount(), productData.getDiscountRate());
  }

  private void checkSellerIsBan(Member seller) {
    if (seller.getIsBan()) throw new CannotSaveProductBecauseMemberBan("벤상태의 회원은 제품등록이 불가능합니다.");
  }

  private void checkMemberAccountIsAvailable(Member seller) {
    if (!seller.checkAccountAvailable())
      throw new MemberAccountIsNotRegistered("제품을 등록하기 전에 회원의 계좌를 등록해야합니다.");
  }

  private void checkMemberIsProductSeller(Product product, Long memberId) {
    if (!product.getSeller().getId().equals(memberId)) {
      throw new DataNotFound("회원에게 해당하는 productId를 가진 제품이 존재하지 않습니다.");
    }
  }
}
