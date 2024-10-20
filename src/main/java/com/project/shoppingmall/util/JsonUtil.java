package com.project.shoppingmall.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shoppingmall.exception.ServerLogicError;

public class JsonUtil {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String convertObjectToJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException ex) {
      throw new ServerLogicError("Json 인코딩 중 에러발생");
    }
  }

  public static <T> T convertJsonToObject(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException ex) {
      System.out.println(ex.getMessage());
      throw new ServerLogicError("Json 디코딩 중 에러발생");
    }
  }
}
