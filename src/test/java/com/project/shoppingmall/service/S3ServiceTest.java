package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.testutil.TestUtil;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@SpringBootTest
class S3ServiceTest {
  @Autowired private S3Service target;
  @Autowired private S3Client s3Client;
  private String givenDirectoryPath;
  private String bucketName;

  @BeforeEach
  public void beforeEach() {
    givenDirectoryPath = "testDirectory/";
    bucketName = ReflectionTestUtils.getField(target, "bucketName").toString();
  }

  @AfterEach
  public void afterEach() {
    resetBucket();
  }

  @Test()
  @DisplayName("S3Service.uploadFile() : 정상흐름")
  public void uploadFile_ok() {
    // given
    MultipartFile inputMockFile =
        TestUtil.loadTestFile("s3TestSampleFile.txt", "static/s3TestSampleFile.txt");
    String inputDirectoryPath = givenDirectoryPath;

    // when
    FileUploadResult fileUploadResult = target.uploadFile(inputMockFile, inputDirectoryPath);

    // then
    checkUploadUri(inputMockFile, fileUploadResult.getFileServerUri());
    checkDownloadUrl(inputMockFile, fileUploadResult.getDownLoadUrl());
  }

  @Test()
  @DisplayName("S3Service.uploadFile() : directoryPath인자 마지막에 '/'가 포함되지 않음")
  public void uploadFile_NoSlashInDirPath() {
    // given
    MultipartFile inputMockFile =
        TestUtil.loadTestFile("s3TestSampleFile.txt", "static/s3TestSampleFile.txt");
    String inputDirectoryPath = givenDirectoryPath.substring(0, givenDirectoryPath.length() - 1);

    // when
    FileUploadResult fileUploadResult = target.uploadFile(inputMockFile, inputDirectoryPath);

    // then
    String serverUriPart = givenDirectoryPath;
    String downloadUrlPart = "/" + bucketName + "/" + givenDirectoryPath;
    checkFileUpdateResult(serverUriPart, downloadUrlPart, fileUploadResult);
  }

  @Test()
  @DisplayName("S3Service.deleteFile() : 정상흐름")
  public void deleteFile_ok() {
    // given
    MultipartFile inputMockFile =
        TestUtil.loadTestFile("s3TestSampleFile.txt", "static/s3TestSampleFile.txt");

    FileUploadResult givenFileUploadResult = target.uploadFile(inputMockFile, givenDirectoryPath);

    // when
    target.deleteFile(givenFileUploadResult.getFileServerUri());

    // then
    assertThrows(
        S3Exception.class,
        () -> downLoadFileContentByServerUri(givenFileUploadResult.getFileServerUri()));
  }

  @Test()
  @DisplayName("S3Service.deleteFile() : 존재하지 않는 파일제거 제거")
  public void deleteFile_NoData() {
    // given
    String wrongServerUri = givenDirectoryPath + "NoData/test.txt";

    // when then
    target.deleteFile(wrongServerUri);
  }

  public void resetBucket() {
    ListObjectsResponse findReq =
        s3Client.listObjects(
            ListObjectsRequest.builder().bucket(bucketName).prefix(givenDirectoryPath).build());
    List<S3Object> contents = findReq.contents();

    ArrayList<ObjectIdentifier> keys = new ArrayList<>();
    for (S3Object content : contents) {
      s3Client.deleteObject(
          DeleteObjectRequest.builder().bucket(bucketName).key(content.key()).build());
    }
  }

  public void checkUploadUri(MultipartFile mockFile, String uploadUri) {
    try {
      String expectedFileContent1 = new String(mockFile.getBytes(), StandardCharsets.UTF_8);
      String savedFileContent1 = downLoadFileContentByServerUri(uploadUri);
      assertEquals(expectedFileContent1, savedFileContent1);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void checkDownloadUrl(MultipartFile mockFile, String downloadUrl) {
    try {
      String expectedFileContent2 = new String(mockFile.getBytes(), StandardCharsets.UTF_8);
      String savedFileContent2 = downLoadFileContentByDownloadUrl(downloadUrl);
      assertEquals(expectedFileContent2, savedFileContent2);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
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

  private void checkFileUpdateResult(
      String serverUriPart, String downloadPart, FileUploadResult target) {
    assertTrue(target.getFileServerUri().contains(serverUriPart));
    assertTrue(target.getDownLoadUrl().contains(downloadPart));
  }
}
