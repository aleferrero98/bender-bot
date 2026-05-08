package com.telegram.bender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SensorsCommandResponseDto {

   @JsonProperty("npu_thermal-virtual-0")
   private ThermalComponentDto npuThermal;

   @JsonProperty("center_thermal-virtual-0")
   private ThermalComponentDto centerThermal;

   @JsonProperty("soc_thermal-virtual-0")
   private ThermalComponentDto socThermal;

   @JsonProperty("nvme-pci-0100")
   private ThermalCompositeComponentDto nvmePci;

   @JsonProperty("gpu_thermal-virtual-0")
   private ThermalComponentDto gpuThermal;

   @JsonProperty("littlecore_thermal-virtual-0")
   private ThermalComponentDto littleCoreThermal;

   @JsonProperty("bigcore0_thermal-virtual-0")
   private ThermalComponentDto bigcore0Thermal;

   @JsonProperty("bigcore1_thermal-virtual-0")
   private ThermalComponentDto bigcore1Thermal;

}
