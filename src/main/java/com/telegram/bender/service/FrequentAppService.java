package com.telegram.bender.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.telegram.bender.model.EFrequentAppStatus;
import com.telegram.bender.model.FrequentAppEntity;
import com.telegram.bender.repository.FrequentAppRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FrequentAppService {

   public static final String CACHE_NAME = "frequentApps";

   private final FrequentAppRepository frequentAppRepository;

   @Cacheable(CACHE_NAME)
   public List<FrequentAppEntity> getEnabledApps() {
      log.debug("Consultando apps frecuentes habilitados en DB");
      return frequentAppRepository.findByStatus(EFrequentAppStatus.ENABLED);
   }

   @Scheduled(fixedRate = 300000)
   @CacheEvict(value = CACHE_NAME, allEntries = true)
   public void evictCache() {
      log.debug("Cache de apps frecuentes invalidada");
   }

   public FrequentAppEntity getByName(String name) {
      return frequentAppRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("App frecuente no encontrada: " + name));
   }
}
