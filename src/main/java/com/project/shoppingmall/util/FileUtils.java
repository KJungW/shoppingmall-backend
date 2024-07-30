package com.project.shoppingmall.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
  public static void sortMultiPartFilesByName(List<MultipartFile> files) {
    Collections.sort(
        files,
        new Comparator<MultipartFile>() {
          @Override
          public int compare(MultipartFile file1, MultipartFile file2) {
            String name1 = file1.getOriginalFilename();
            String name2 = file2.getOriginalFilename();

            if (name1 == null && name2 == null) return 0;
            if (name1 == null && name2 != null) return -1;
            if (name1 != null && name2 == null) return 1;

            return name1.compareTo(name2);
          }
        });
  }
}
