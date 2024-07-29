package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.file.FileUploadResult;
import com.project.shoppingmall.exception.FileDeleteFail;
import com.project.shoppingmall.exception.FileUploadFail;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class S3Service {
  private final S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  @Transactional
  public FileUploadResult uploadFile(MultipartFile file, String directoryPath) {
    try {
      String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
      String serverUri = makeServerFileName(directoryPath, extension);

      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .contentType(file.getContentType())
              .contentLength(file.getSize())
              .key(serverUri)
              .build();
      RequestBody requestBody = RequestBody.fromBytes(file.getBytes());
      s3Client.putObject(putObjectRequest, requestBody);

      GetUrlRequest getUrlRequest =
          GetUrlRequest.builder().bucket(bucketName).key(serverUri).build();
      String downloadUrl = s3Client.utilities().getUrl(getUrlRequest).toString();
      return new FileUploadResult(serverUri, downloadUrl);
    } catch (IOException ex) {
      throw new FileUploadFail("파일 업로드 실패");
    }
  }

  @Transactional
  public void deleteFile(String serverUri) {
    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucketName).key(serverUri).build();
      s3Client.deleteObject(deleteObjectRequest);
    } catch (Exception ex) {
      throw new FileDeleteFail("파일 삭제 실패");
    }
  }

  private String makeServerFileName(String directoryUrl, String extension) {
    if (directoryUrl.endsWith("/")) {
      return directoryUrl + UUID.randomUUID().toString() + "." + extension;
    } else {
      return directoryUrl + "/" + UUID.randomUUID().toString() + "." + extension;
    }
  }
}
