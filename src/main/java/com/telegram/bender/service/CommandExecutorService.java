package com.telegram.bender.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bender.dto.SensorsCommandResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandExecutorService {

   public String executeFastfetch() {
      try {
         Process process = new ProcessBuilder(
               "fastfetch",
               "--logo", "none",
               "--pipe",
               "--structure", "os:host:kernel:uptime:packages:shell:display:de:wm:wmtheme:theme:icons:font:cursor:terminal:terminalfont:cpu:gpu:memory:swap:disk:localip:battery:poweradapter:locale"
         ).start();
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
         }
      } catch (Exception ex) {
         return "Error al ejecutar fastfetch: " + ex.getMessage();
      }
   }

   public String executeSensors() {
      try {
         Process process = new ProcessBuilder("sensors", "-j").start();
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String json = reader.lines().collect(Collectors.joining("\n"));

            ObjectMapper mapper = new ObjectMapper();
            SensorsCommandResponseDto dto = mapper.readValue(json, SensorsCommandResponseDto.class);

            return dto.toFormattedString();
         }
      } catch (Exception ex) {
         return "Error al ejecutar fastfetch: " + ex.getMessage();
      }
   }

   public boolean processExists(int pid) {
      try {
         return new java.io.File("/proc/" + pid).exists();
      } catch (Exception ex) {
         log.error("Error al verificar /proc: " + ex.getMessage());

         return false;
      }
   }

   public String killProcessSafe(int pid) {
      if (!processExists(pid)) {
         return "El proceso " + pid + " no existe";
      }

      try {

         Process process = new ProcessBuilder("kill", String.valueOf(pid)).start();
         int exitCode = process.waitFor();

         if (exitCode == 0) {
            return "Proceso " + pid + " terminado exitosamente";
         }

         process = new ProcessBuilder("kill", "-9", String.valueOf(pid)).start();
         exitCode = process.waitFor();

         if (exitCode == 0) {
            return "Proceso " + pid + " terminado forzosamente";
         } else {
            return "No se pudo terminar el proceso " + pid;
         }

      } catch (Exception ex) {
         return "Error al ejecutar kill: " + ex.getMessage();
      }
   }

   public boolean executeTurnOffLeds() {
      try {
         Process process = new ProcessBuilder("sudo", "turn-off-leds").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error al ejecutar turn-off-leds: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeReboot() {
      try {
         Process process = new ProcessBuilder("sudo", "reboot", "now").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error al ejecutar reboot: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeShutdown() {
      try {
         Process process = new ProcessBuilder("sudo", "shutdown", "now").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error al ejecutar shutdown: {}", ex.getMessage());
         return false;
      }
   }

   public boolean setCoolerSpeed(int speed) {
      try {
         Process process = new ProcessBuilder("sudo", "fan", String.valueOf(speed)).start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error al setear velocidad del cooler: {}", ex.getMessage());
         return false;
      }
   }

   public boolean enableTempController() {
      try {
         Process process = new ProcessBuilder("sudo", "systemctl", "start", "temperature_controller.service").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error al iniciar temperature_controller: {}", ex.getMessage());
         return false;
      }
   }

   public boolean disableTempController() {
      try {
         Process process = new ProcessBuilder("sudo", "systemctl", "stop", "temperature_controller.service").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error al deshabilitar temperature_controller: {}", ex.getMessage());
         return false;
      }
   }

   public boolean isTempControllerRunning() {
      try {
         Process process = new ProcessBuilder("sudo", "systemctl", "is-active", "temperature_controller.service").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error al verificar estado de temperature_controller: {}", ex.getMessage());
         return false;
      }
   }

}
