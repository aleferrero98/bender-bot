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

   public String toFormattedString() {
      StringBuilder sb = new StringBuilder();

      appendIfNotNull(sb, "NPU", npuThermal);
      appendIfNotNull(sb, "Center", centerThermal);
      appendIfNotNull(sb, "SoC", socThermal);
      appendIfNotNull(sb, "GPU", gpuThermal);
      appendIfNotNull(sb, "Little Core", littleCoreThermal);
      appendIfNotNull(sb, "Big Core 0", bigcore0Thermal);
      appendIfNotNull(sb, "Big Core 1", bigcore1Thermal);
      appendIfNotNull(sb, "NVMe", nvmePci);

      return sb.toString();
   }

   private void appendIfNotNull(StringBuilder sb, String label, ThermalComponentDto dto) {
      if (dto != null && dto.getTemperature() != null) {
         sb.append(String.format("- *%s*: %.1f°C (🔥 %.0f°C)\n",
               label,
               dto.getTemperature().getTemperatureInput(),
               dto.getTemperature().getTemperatureCritical()));
      }
   }

   private void appendIfNotNull(StringBuilder sb, String label, ThermalCompositeComponentDto dto) {
      if (dto != null && dto.getCompositeTemperature() != null) {
         sb.append(String.format("- *%s*: %.1f°C (🔥 %.0f°C)\n",
               label,
               dto.getCompositeTemperature().getTemperatureInput(),
               dto.getCompositeTemperature().getTemperatureCritical()));
      }
   }

}
