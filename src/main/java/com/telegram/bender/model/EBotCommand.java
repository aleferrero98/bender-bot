package com.telegram.bender.model;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EBotCommand {

   START("start", "Iniciar el bot y ver el menú principal"),
   INFO("info", "Consultar información y temperatura del servidor"),
   COOLER("cooler", "Configurar la velocidad del cooler"),
   TUNNEL("tunnel", "Listar túneles SSH activos en el servidor"),
   MANAGE("manage", "Administrar el servidor (reboot, shutdown, LEDs)"),
   HELP("help", "Mostrar lista de comandos disponibles");

   private final String name;

   private final String description;

   public static boolean isValidCommand(String command) {
      return Arrays.stream(values()).anyMatch(c -> c.getName().equals(command));
   }

}
