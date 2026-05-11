package com.telegram.bender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThermalCompositeComponentDto {

   @JsonProperty("Adapter")
   private String adapter;

   @JsonProperty("Composite")
   private TemperatureDataDto compositeTemperature;

}
