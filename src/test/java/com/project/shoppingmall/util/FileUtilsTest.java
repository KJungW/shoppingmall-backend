package com.project.shoppingmall.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class FileUtilsTest {

  @Test
  @DisplayName("sortMultiPartFilesByName() : 정상흐름")
  public void compare_ok() throws IOException {
    // given
    MockMultipartFile givenProductImage1 =
        new MockMultipartFile(
            "file",
            "1.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/1.png")));
    MockMultipartFile givenProductImage2 =
        new MockMultipartFile(
            "file",
            "2.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/2.png")));
    MockMultipartFile givenProductImage3 =
        new MockMultipartFile(
            "file",
            "3.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/3.png")));

    List<MultipartFile> sortTarget = new ArrayList<>();
    sortTarget.add(givenProductImage3);
    sortTarget.add(givenProductImage2);
    sortTarget.add(givenProductImage1);

    // when
    FileUtils.sortMultiPartFilesByName(sortTarget);

    // then
    assertEquals(3, sortTarget.size());
    assertEquals(givenProductImage1.getOriginalFilename(), sortTarget.get(0).getOriginalFilename());
    assertEquals(givenProductImage2.getOriginalFilename(), sortTarget.get(1).getOriginalFilename());
    assertEquals(givenProductImage3.getOriginalFilename(), sortTarget.get(2).getOriginalFilename());
  }

  @Test
  @DisplayName("sortMultiPartFilesByName() : 동일 파일 입력")
  public void compare_SameFileInput() throws IOException {
    // given
    MockMultipartFile givenProductImage1 =
        new MockMultipartFile(
            "file",
            "1.png",
            "image/png",
            Files.readAllBytes(Paths.get("src/test/resources/static/product_image/1.png")));

    List<MultipartFile> sortTarget = new ArrayList<>();
    sortTarget.add(givenProductImage1);
    sortTarget.add(givenProductImage1);
    sortTarget.add(givenProductImage1);

    // when
    FileUtils.sortMultiPartFilesByName(sortTarget);

    // then
    assertEquals(3, sortTarget.size());
    assertEquals(givenProductImage1.getOriginalFilename(), sortTarget.get(0).getOriginalFilename());
    assertEquals(givenProductImage1.getOriginalFilename(), sortTarget.get(1).getOriginalFilename());
    assertEquals(givenProductImage1.getOriginalFilename(), sortTarget.get(2).getOriginalFilename());
  }
}
