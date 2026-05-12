package com.telegram.bender.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.telegram.bender.dto.UpdateShortUrlRequestDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShortIoClient {

   private static final String UPDATE_SHORT_URL = "/links/%s?domain_id=%d";

   private final String baseUrl;
   private final String token;
   private final Long domainId;
   private final String benderLinkId;
   private final String immichLinkId;
   private final String nextcloudLinkId;
   private final String spliitLinkId;
   private final String pingvinShareLinkId;
   private final WebClient webClient;

   public ShortIoClient(@Value("${spring.application.short-io.url}") String baseUrl,
         @Value("${spring.application.short-io.token}") String token,
         @Value("${spring.application.short-io.domain-id}") Long domainId,
         @Value("${spring.application.short-io.link-id.bender}") String benderLinkId,
         @Value("${spring.application.short-io.link-id.immich}") String immichLinkId,
         @Value("${spring.application.short-io.link-id.nextcloud}") String nextcloudLinkId,
         @Value("${spring.application.short-io.link-id.spliit}") String spliitLinkId,
         @Value("${spring.application.short-io.link-id.pingvin-share}") String pingvinShareLinkId) {
      this.baseUrl = baseUrl;
      this.token = token;
      this.domainId = domainId;
      this.benderLinkId = benderLinkId;
      this.immichLinkId = immichLinkId;
      this.nextcloudLinkId = nextcloudLinkId;
      this.spliitLinkId = spliitLinkId;
      this.pingvinShareLinkId = pingvinShareLinkId;

      this.webClient = WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, token)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
   }

   public void updateShortUrl(String shortLinkId, String newDestinationUrl) {
      try {
         String uri = String.format(UPDATE_SHORT_URL, shortLinkId, domainId);
         webClient.post()
               .uri(uri)
               .bodyValue(new UpdateShortUrlRequestDto(newDestinationUrl))
               .retrieve()
               .bodyToMono(Void.class)
               .block();
         log.info("Short URL updated successfully: shortLinkId={}", shortLinkId);
      } catch (Exception ex) {
         log.error("Error updating short URL {}: {}", shortLinkId, ex.getMessage());
      }
   }
}
