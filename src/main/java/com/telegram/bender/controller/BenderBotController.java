package com.telegram.bender.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegram.bender.service.FrequentAppService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bender/v1")
public class BenderBotController {

   private final FrequentAppService frequentAppService;

   @GetMapping("/health")
   public ResponseEntity<?> getHealth() {
      return ResponseEntity.ok("OK");
   }

   @PostMapping("/frequent-apps/cache/evict")
   public ResponseEntity<?> evictFrequentAppsCache() {
      frequentAppService.evictCache();
      return ResponseEntity.ok("frequentApps cache invalidated");
   }

}
