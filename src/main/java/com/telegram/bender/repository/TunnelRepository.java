package com.telegram.bender.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telegram.bender.model.ETunnelStatus;
import com.telegram.bender.model.TunnelEntity;

@Repository
public interface TunnelRepository extends JpaRepository<TunnelEntity, Long> {

   List<TunnelEntity> findByStatus(ETunnelStatus status);

   List<TunnelEntity> findByStatusAndExpiresAtBefore(ETunnelStatus status, LocalDateTime dateTime);
}
