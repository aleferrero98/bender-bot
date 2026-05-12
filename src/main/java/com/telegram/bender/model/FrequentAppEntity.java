package com.telegram.bender.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "frequent_app")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentAppEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 64)
   private String name;

   @Column(nullable = false)
   private Integer port;

   @Column(name = "short_io_url", nullable = false, length = 128)
   private String shortIoUrl;

   @Column(name = "short_io_link_id", nullable = false, length = 64)
   private String shortIoLinkId;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false, columnDefinition = "ENUM('ENABLED','DISABLED') DEFAULT 'ENABLED'")
   @Builder.Default
   private EFrequentAppStatus status = EFrequentAppStatus.ENABLED;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "tunnel_id")
   private TunnelEntity tunnel;

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
