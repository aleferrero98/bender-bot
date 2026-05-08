package com.telegram.bender.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bender/v1")
public class BenderBotController {

   @GetMapping("/health")
   public ResponseEntity<?> getHealth() {
      return ResponseEntity.ok("OK");
   }

}
