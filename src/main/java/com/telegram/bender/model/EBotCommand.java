package com.telegram.bender.model;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EBotCommand {

   START("start", "Iniciar el bot y ver el menú principal"),
   INFO("info", "Consultar información del servidor"),
   TEMPERATURE("temperature", "Consultar temperaturas del servidor"),
   TUNNEL("tunnel", "Listar túneles SSH activos en el servidor"),
   TURN_OFF_LEDS("turn_off_leds", "Apagar los LEDs del servidor"),
   REBOOT("reboot", "Reiniciar el servidor"),
   SHUTDOWN("shutdown", "Apagar el servidor"),
   COOLER("cooler", "Configurar la velocidad del cooler"),
   HELP("help", "Mostrar lista de comandos disponibles");

   private final String name;

   private final String description;

   public static boolean isValidCommand(String command) {
      return Arrays.stream(values()).anyMatch(c -> c.getName().equals(command));
   }

}
