package com.telegram.bender.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tunnel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TunnelEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 256)
   private String url;

   @Column(name = "exposed_port", nullable = false)
   private Integer exposedPort;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false, columnDefinition = "ENUM('ACTIVE','CANCELLED','EXPIRED','ERROR') DEFAULT 'ACTIVE'")
   @Builder.Default
   private ETunnelStatus status = ETunnelStatus.ACTIVE;

   @Column(name = "expires_at", nullable = false)
   private LocalDateTime expiresAt;

   @Column(name = "process_id", nullable = false)
   private Integer processId;

   @Column(name = "created_at", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   @Column(name = "updated_at")
   private LocalDateTime updatedAt;

   @PrePersist
   protected void onCreate() {
      createdAt = LocalDateTime.now();
   }

   @PreUpdate
   protected void onUpdate() {
      updatedAt = LocalDateTime.now();
   }
}
