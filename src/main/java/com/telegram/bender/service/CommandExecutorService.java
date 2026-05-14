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
         return "Error executing fastfetch: " + ex.getMessage();
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
         return "Error executing sensors: " + ex.getMessage();
      }
   }

   public boolean processExists(int pid) {
      try {
         return new java.io.File("/proc/" + pid).exists();
      } catch (Exception ex) {
         log.error("Error checking /proc: {}", ex.getMessage());
         return false;
      }
   }

   public void killProcess(int pid) throws Exception {
      if (!processExists(pid)) {
         log.warn("Process {} does not exist, skipping kill", pid);
         return;
      }

      try {
         Process process = new ProcessBuilder("kill", String.valueOf(pid)).start();
         int exitCode = process.waitFor();

         if (exitCode == 0) {
            log.info("Process {} terminated successfully", pid);
            return;
         }

         log.warn("Graceful kill failed for process {}, attempting force kill", pid);
         process = new ProcessBuilder("kill", "-9", String.valueOf(pid)).start();
         exitCode = process.waitFor();

         if (exitCode != 0) {
            log.error("Failed to kill process {} even with SIGKILL", pid);
            throw new Exception("Failed to kill process " + pid);
         }

         log.info("Process {} terminated with SIGKILL", pid);
      } catch (Exception ex) {
         log.error("Error killing process {}: {}", pid, ex.getMessage());
         throw new Exception("Error killing process " + pid + ": " + ex.getMessage(), ex);
      }
   }

   public boolean executeTurnOffLeds() {
      try {
         Process process = new ProcessBuilder("sudo", "turn-off-leds").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error executing turn-off-leds: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeReboot() {
      try {
         Process process = new ProcessBuilder("sudo", "reboot", "now").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error executing reboot: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeShutdown() {
      try {
         Process process = new ProcessBuilder("sudo", "shutdown", "now").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error executing shutdown: {}", ex.getMessage());
         return false;
      }
   }

   public boolean setCoolerSpeed(int speed) {
      try {
         Process process = new ProcessBuilder("sudo", "fan", String.valueOf(speed)).start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error setting cooler speed: {}", ex.getMessage());
         return false;
      }
   }

   public boolean enableTempController() {
      try {
         Process process = new ProcessBuilder("sudo", "systemctl", "start", "temperature_controller.service").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error starting temperature_controller: {}", ex.getMessage());
         return false;
      }
   }

   public boolean disableTempController() {
      try {
         Process process = new ProcessBuilder("sudo", "systemctl", "stop", "temperature_controller.service").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error stopping temperature_controller: {}", ex.getMessage());
         return false;
      }
   }

   public boolean isTempControllerRunning() {
      try {
         Process process = new ProcessBuilder("sudo", "systemctl", "is-active", "temperature_controller.service").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error checking temperature_controller status: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeImmichBackup() {
      try {
         Process process = new ProcessBuilder("immich-borg-backup").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error executing immich-borg-backup: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeNextcloudBackup() {
      try {
         Process process = new ProcessBuilder("nextcloud-borg-backup").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error executing nextcloud-borg-backup: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeDockerBackup() {
      try {
         Process process = new ProcessBuilder("docker-borg-backup").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error executing docker-borg-backup: {}", ex.getMessage());
         return false;
      }
   }

   public boolean executeColdBackup() {
      try {
         Process process = new ProcessBuilder("external-disk-backup").start();
         return process.waitFor() == 0;
      } catch (Exception ex) {
         log.error("Error executing external-disk-backup: {}", ex.getMessage());
         return false;
      }
   }

   public Process executeCloudflaredTunnel(int port) throws Exception {
      try {
         Process process = new ProcessBuilder(
               "cloudflared", "tunnel", "--url", "http://localhost:" + port
         ).start();
         log.info("Cloudflared tunnel started on port {} with PID {}", port, process.pid());
         return process;
      } catch (Exception ex) {
         log.error("Error starting cloudflared tunnel on port {}: {}", port, ex.getMessage());
         throw new Exception("Error starting cloudflared tunnel: " + ex.getMessage(), ex);
      }
   }
}
