package com.telegram.bender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateShortUrlRequestDto {

   @JsonProperty("originalURL")
   private String originalUrl;

}
