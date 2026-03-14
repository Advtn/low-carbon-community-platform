package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.RedemptionOrder;
import com.lowcarbon.platform.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RedemptionOrderRepository extends JpaRepository<RedemptionOrder, Long> {

    List<RedemptionOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<RedemptionOrder> findAllByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    long countByStatusAndCompletedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
