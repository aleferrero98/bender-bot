package com.telegram.bender.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TunnelExpirationScheduler {

   private final TunnelService tunnelService;

   // Every 10 min
   @Scheduled(fixedRate = 600000)
   public void expireTunnels() {
      log.info("Running tunnel expiration check...");
      tunnelService.expireTunnels();
   }
}
