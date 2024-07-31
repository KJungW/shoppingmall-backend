package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.block.ContentBlock;
import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.dto.block.ImageBlockBeforeImageSave;
import com.project.shoppingmall.dto.block.TextBlock;
import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.dto.product.ProductMakeData;
import com.project.shoppingmall.dto.product.ProductOption;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.InvalidEnumType;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.JsonUtil;
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
  private final MemberService memberService;
  private final ProductTypeService productTypeService;
  private final ProductRepository productRepository;
  private final S3Service s3Service;
  private final JsonUtil jsonUtil;

  @Transactional
  public Product save(Long memberId, ProductMakeData productData) {
    Member seller =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    ProductType productType =
        productTypeService
            .findById(productData.getProductTypeId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 제품타입이 존재하지 않습니다."));

    Product newProduct =
        Product.builder()
            .seller(seller)
            .productType(productType)
            .name(productData.getName())
            .price(productData.getPrice())
            .discountAmount(productData.getDiscountAmount())
            .discountRate(productData.getDiscountRate())
            .isBan(false)
            .build();

    ArrayList<ProductImage> productImages =
        makeProductImageList(productData.getProductImages(), "productImage/" + seller.getId());
    ArrayList<ProductContent> productContents =
        makeProductContentsList(productData.getContentBlocks(), "blockImage/" + seller.getId());
    ProductSingleOption productSingleOption =
        makeProductSingleOption(productData.getSingleOption());
    List<ProductMultipleOption> productMultipleOptions =
        makeProductMultipleOptionList(productData.getMultiOptions());

    newProduct.updateProductImages(productImages);
    newProduct.updateContents(productContents);
    newProduct.updateSingleOption(productSingleOption);
    newProduct.updateMultiOptions(productMultipleOptions);

    productRepository.save(newProduct);
    return newProduct;
  }

  @Transactional
  public void update(Long memberId, Long productId, ProductMakeData productData) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    ProductType productType =
        productTypeService
            .findById(productData.getProductTypeId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 제품타입이 존재하지 않습니다."));
    if (!validateMemberIsProductSeller(product, memberId)) {
      throw new DataNotFound("회원에게 해당하는 productId를 가진 제품이 존재하지 않습니다.");
    }

    removeOriginProductImages(product);
    removeOriginProductContentImage(product);

    ArrayList<ProductImage> productImages =
        makeProductImageList(
            productData.getProductImages(), "productImage/" + product.getSeller().getId());
    ArrayList<ProductContent> productContents =
        makeProductContentsList(
            productData.getContentBlocks(), "blockImage/" + product.getSeller().getId());
    ProductSingleOption productSingleOption =
        makeProductSingleOption(productData.getSingleOption());
    List<ProductMultipleOption> productMultipleOptions =
        makeProductMultipleOptionList(productData.getMultiOptions());

    product.changeProductType(productType);
    product.changeProductName(productData.getName());
    product.changePrice(
        productData.getPrice(), productData.getDiscountAmount(), productData.getDiscountRate());

    product.updateProductImages(productImages);
    product.updateContents(productContents);
    product.updateMultiOptions(productMultipleOptions);
    product.updateSingleOption(productSingleOption);
  }

  public boolean validateMemberIsProductSeller(Product product, Long memberId) {
    return product.getSeller().getId().equals(memberId);
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
            jsonUtil.convertJsonToObject(productContent.getContent(), ImageBlock.class);
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
        .content(jsonUtil.convertObjectToJson(textBlock))
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
        .content(jsonUtil.convertObjectToJson(imageBlock))
        .build();
  }

  private ProductSingleOption makeProductSingleOption(ProductOption option) {
    return ProductSingleOption.builder()
        .optionName(option.getOptionName())
        .priceChangeAmount(option.getPriceChangeAmount())
        .build();
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
}
