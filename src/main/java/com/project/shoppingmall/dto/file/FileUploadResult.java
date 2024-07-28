package com.project.shoppingmall.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadResult {
  private String fileServerUri;
  private String downLoadUrl;
}
