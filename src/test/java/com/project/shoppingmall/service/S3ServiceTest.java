package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.file.FileUploadResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@SpringBootTest
class S3ServiceTest {
  @Autowired private S3Service target;
  @Autowired private S3Client s3Client;
  private String givenTestDirectoryPath;
  private String bucketName;

  @BeforeEach
  public void beforeEach() {
    givenTestDirectoryPath = "testDirectory/";
    bucketName = ReflectionTestUtils.getField(target, "bucketName").toString();
  }

  @AfterEach
  public void afterEach() {
    resetBucket();
  }

  @Test()
  @DisplayName("S3Service.uploadFile() : 정상흐름")
  public void uploadFile_ok() throws IOException {
    // given
    MultipartFile givenMockFile =
        new MockMultipartFile(
            "s3TestSampleFile.txt",
            new FileInputStream(new ClassPathResource("static/s3TestSampleFile.txt").getFile()));

    // when
    FileUploadResult fileUploadResult = target.uploadFile(givenMockFile, givenTestDirectoryPath);

    // then
    // fileUploadResult.fileServerUri 검증
    String expectedFileContent1 = new String(givenMockFile.getBytes(), StandardCharsets.UTF_8);
    String savedFileContent1 = downLoadFileContentByServerUri(fileUploadResult.getFileServerUri());
    assertEquals(expectedFileContent1, savedFileContent1);

    // fileUploadResult.downLoadUrl 검증
    String expectedFileContent2 = new String(givenMockFile.getBytes(), StandardCharsets.UTF_8);
    String savedFileContent2 = downLoadFileContentByDownloadUrl(fileUploadResult.getDownLoadUrl());
    assertEquals(expectedFileContent2, savedFileContent2);
  }

  @Test()
  @DisplayName("S3Service.uploadFile() : directoryPath인자 마지막에 '/'가 포함되지 않음")
  public void uploadFile_NoSlashInDirPath() throws IOException {
    // given
    String givenWrongTestDirectoryPath =
        givenTestDirectoryPath.substring(0, givenTestDirectoryPath.length() - 1);
    MultipartFile givenMockFile =
        new MockMultipartFile(
            "s3TestSampleFile.txt",
            new FileInputStream(new ClassPathResource("static/s3TestSampleFile.txt").getFile()));

    // when
    FileUploadResult fileUploadResult =
        target.uploadFile(givenMockFile, givenWrongTestDirectoryPath);

    // then
    assertTrue(fileUploadResult.getFileServerUri().contains(givenTestDirectoryPath));
    assertTrue(
        fileUploadResult
            .getDownLoadUrl()
            .contains("/" + bucketName + "/" + givenTestDirectoryPath));
  }

  @Test()
  @DisplayName("S3Service.deleteFile() : 정상흐름")
  public void deleteFile_ok() throws IOException {
    // given
    MultipartFile givenMockFile =
        new MockMultipartFile(
            "s3TestSampleFile.txt",
            new FileInputStream(new ClassPathResource("static/s3TestSampleFile.txt").getFile()));
    FileUploadResult givenFileUploadResult =
        target.uploadFile(givenMockFile, givenTestDirectoryPath);

    target.deleteFile(givenFileUploadResult.getFileServerUri());

    // when then
    assertThrows(
        S3Exception.class,
        () -> {
          downLoadFileContentByServerUri(givenFileUploadResult.getFileServerUri());
        });
  }

  @Test()
  @DisplayName("S3Service.deleteFile() : 존재하지 않는 파일제거 제거")
  public void deleteFile_NoData() throws IOException {
    // given
    String wrongServerUri = givenTestDirectoryPath + "NoData/test.txt";

    // when then
    target.deleteFile(wrongServerUri);
  }

  public void resetBucket() {
    // 테스트 디렉토리의 모든 파일조회
    ListObjectsResponse findReq =
        s3Client.listObjects(
            ListObjectsRequest.builder().bucket(bucketName).prefix(givenTestDirectoryPath).build());
    List<S3Object> contents = findReq.contents();

    // 조회된 모든 파일제거
    ArrayList<ObjectIdentifier> keys = new ArrayList<>();
    for (S3Object content : contents) {
      s3Client.deleteObject(
          DeleteObjectRequest.builder().bucket(bucketName).key(content.key()).build());
    }
  }

  private String downLoadFileContentByServerUri(String serverUri) {
    ResponseBytes<GetObjectResponse> savedFileObject =
        s3Client.getObjectAsBytes(
            GetObjectRequest.builder().bucket(bucketName).key(serverUri).build());
    return new String(savedFileObject.asByteArray(), StandardCharsets.UTF_8);
  }

  private String downLoadFileContentByDownloadUrl(String downloadUrl) throws IOException {
    URL url = new URL(downloadUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    StringBuilder content = new StringBuilder();
    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      content.append(line);
    }
    return content.toString();
  }
}
