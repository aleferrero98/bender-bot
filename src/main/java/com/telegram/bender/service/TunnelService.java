package com.telegram.bender.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telegram.bender.model.ETunnelStatus;
import com.telegram.bender.model.FrequentAppEntity;
import com.telegram.bender.model.TunnelEntity;
import com.telegram.bender.repository.FrequentAppRepository;
import com.telegram.bender.repository.TunnelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TunnelService {

   private static final Pattern URL_PATTERN = Pattern.compile("https://[a-zA-Z0-9-]+\\.trycloudflare\\.com");
   private static final int URL_EXTRACTION_TIMEOUT_SECONDS = 10;

   private final CommandExecutorService commandExecutorService;
   private final ShortIoClient shortIoClient;
   private final TunnelRepository tunnelRepository;
   private final FrequentAppRepository frequentAppRepository;

   public List<TunnelEntity> getActiveTunnels() {
      return tunnelRepository.findByStatus(ETunnelStatus.ACTIVE);
   }

   @Transactional
   public TunnelEntity createTunnel(int port, int durationMinutes) throws Exception {
      Process process = commandExecutorService.executeCloudflaredTunnel(port);

      int pid = (int) process.pid();

      String url;
      try {
         url = extractUrlFromProcess(process);
      } catch (Exception ex) {
         process.destroyForcibly();
         log.error("Failed to extract URL from cloudflared process: {}", ex.getMessage());
         throw new Exception("Failed to create tunnel: " + ex.getMessage(), ex);
      }

      LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);

      TunnelEntity tunnel = TunnelEntity.builder()
            .url(url)
            .exposedPort(port)
            .status(ETunnelStatus.ACTIVE)
            .expiresAt(expiresAt)
            .processId(pid)
            .build();

      log.info("Tunnel created: id={}, url={}, port={}, expiresAt={}", tunnel.getId(), url, port, expiresAt);
      return tunnelRepository.save(tunnel);
   }

   @Transactional
   public TunnelEntity createFrequentAppTunnel(String appName, int durationMinutes) throws Exception {
      FrequentAppEntity app = frequentAppRepository.findByName(appName)
            .orElseThrow(() -> new Exception("Frequent app not found: " + appName));

      if (app.getTunnel() != null && app.getTunnel().getStatus() == ETunnelStatus.ACTIVE) {
         log.info("Reusing existing tunnel for app {}: tunnelId={}", appName, app.getTunnel().getId());
         return app.getTunnel();
      }

      TunnelEntity tunnel = createTunnel(app.getPort(), durationMinutes);

      app.setTunnel(tunnel);
      frequentAppRepository.save(app);

      shortIoClient.updateShortUrl(app.getShortIoLinkId(), tunnel.getUrl());

      return tunnel;
   }

   @Transactional
   public void cancelTunnel(Integer tunnelId) throws Exception {
      TunnelEntity tunnel = tunnelRepository.findById(tunnelId)
            .orElseThrow(() -> new Exception("Tunnel not found: " + tunnelId));

      if (tunnel.getStatus() != ETunnelStatus.ACTIVE) {
         throw new Exception("Tunnel is not active");
      }

      try {
         commandExecutorService.killProcess(tunnel.getProcessId());
         tunnel.setStatus(ETunnelStatus.CANCELLED);
         log.info("Tunnel {} cancelled successfully", tunnelId);
      } catch (Exception ex) {
         tunnel.setStatus(ETunnelStatus.ERROR);
         log.error("Error cancelling tunnel {}: {}", tunnelId, ex.getMessage());
         throw new Exception("Error cancelling tunnel: " + ex.getMessage(), ex);
      } finally {
         tunnelRepository.save(tunnel);
      }
   }

   @Transactional
   public void expireTunnels() {
      List<TunnelEntity> expiredTunnels = tunnelRepository.findByStatusAndExpiresAtBefore(
            ETunnelStatus.ACTIVE, LocalDateTime.now());

      for (TunnelEntity tunnel : expiredTunnels) {
         log.info("Expiring tunnel: id={}, url={}, port={}", tunnel.getId(), tunnel.getUrl(), tunnel.getExposedPort());

         try {
            commandExecutorService.killProcess(tunnel.getProcessId());
            tunnel.setStatus(ETunnelStatus.EXPIRED);
            log.info("Tunnel {} expired successfully", tunnel.getId());
         } catch (Exception ex) {
            tunnel.setStatus(ETunnelStatus.ERROR);
            log.error("Error expiring tunnel {}: {}", tunnel.getId(), ex.getMessage());
         }

         tunnelRepository.save(tunnel);
      }
   }

   private String extractUrlFromProcess(Process process) throws Exception {
      CompletableFuture<String> urlFuture = new CompletableFuture<>();
      StringBuilder stderrOutput = new StringBuilder();

      Thread readerThread = new Thread(() -> {
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
               stderrOutput.append(line).append("\n");
               Matcher matcher = URL_PATTERN.matcher(line);
               if (matcher.find()) {
                  urlFuture.complete(matcher.group());
                  break;
               }
            }
         } catch (Exception ex) {
            log.error("Error reading cloudflared output: {}", ex.getMessage());
         }
      });

      readerThread.setDaemon(true);
      readerThread.start();

      long deadline = System.currentTimeMillis() + (URL_EXTRACTION_TIMEOUT_SECONDS * 1000L);
      while (System.currentTimeMillis() < deadline) {
         if (urlFuture.isDone()) {
            return urlFuture.get();
         }
         if (!process.isAlive()) {
            int exitCode = process.exitValue();
            log.error("Cloudflared exited prematurely with code {}. Stderr:\n{}", exitCode, stderrOutput);
            throw new Exception("Cloudflared exited with code " + exitCode + ": " + stderrOutput.toString().trim());
         }
         Thread.sleep(200);
      }

      process.destroyForcibly();
      log.error("Timeout waiting for tunnel URL. Stderr:\n{}", stderrOutput);
      throw new Exception("Timeout waiting for tunnel URL. Cloudflared output:\n" + stderrOutput.toString().trim());
   }
}
