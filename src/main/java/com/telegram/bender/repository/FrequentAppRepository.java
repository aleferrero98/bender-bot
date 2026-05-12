package com.telegram.bender.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telegram.bender.model.EFrequentAppStatus;
import com.telegram.bender.model.FrequentAppEntity;

@Repository
public interface FrequentAppRepository extends JpaRepository<FrequentAppEntity, Integer> {

   Optional<FrequentAppEntity> findByName(String name);

   List<FrequentAppEntity> findByStatus(EFrequentAppStatus status);
}
