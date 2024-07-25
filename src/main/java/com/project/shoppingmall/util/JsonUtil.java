package com.project.shoppingmall.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonUtil {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public String convertObjectToJson(Object obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }
}
