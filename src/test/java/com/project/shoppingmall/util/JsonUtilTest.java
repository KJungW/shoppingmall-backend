package com.project.shoppingmall.util;

import static org.junit.jupiter.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

  @Test
  @DisplayName("JsonUtil.convertObjectToJson() / convertJsonToObject : 정상흐름")
  public void jsonEncodeAndDecodeTest() {
    // given
    Long givenId = 1L;
    String givenTitle = "testTitle";
    String givenContent = "testContent";
    JsonTestDto givenDto = new JsonTestDto(givenId, givenTitle, givenContent);

    // when
    String resultJson = JsonUtil.convertObjectToJson(givenDto);
    JsonTestDto resultDto = JsonUtil.convertJsonToObject(resultJson, JsonTestDto.class);

    // then
    assertEquals(givenId, resultDto.getId());
    assertEquals(givenTitle, resultDto.getTitle());
    assertEquals(givenContent, resultDto.getContent());
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  static class JsonTestDto {
    private Long id;
    private String title;
    private String content;
  }
}
