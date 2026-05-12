package com.telegram.bender.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telegram.bender.model.FrequentServiceEntity;

@Repository
public interface FrequentServiceRepository extends JpaRepository<FrequentServiceEntity, Long> {

   Optional<FrequentServiceEntity> findByName(String name);
}
