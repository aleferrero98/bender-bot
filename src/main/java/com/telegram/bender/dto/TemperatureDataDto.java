package com.telegram.bender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TemperatureDataDto {

   @JsonProperty("temp1_input")
   private Double temperatureInput;

   @JsonProperty("temp1_crit")
   private Double temperatureCritical;

   @JsonProperty("temp1_max")
   private Double temperatureMax;

   @JsonProperty("temp1_min")
   private Double temperatureMin;

   @JsonProperty("temp1_alarm")
   private Double temperatureAlarm;

}
