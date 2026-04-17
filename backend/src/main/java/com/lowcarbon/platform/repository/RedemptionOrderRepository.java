package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.RedemptionOrder;
import com.lowcarbon.platform.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RedemptionOrderRepository {

    List<RedemptionOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<RedemptionOrder> findAllByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    long countByStatusAndCompletedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Optional<RedemptionOrder> findById(Long id);

    RedemptionOrder save(RedemptionOrder order);
}
