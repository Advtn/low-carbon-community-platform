package com.lowcarbon.platform.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lowcarbon.platform.entity.RedemptionOrder;
import com.lowcarbon.platform.enums.OrderStatus;
import com.lowcarbon.platform.mapper.RedemptionOrderMapper;
import com.lowcarbon.platform.repository.RedemptionOrderRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class RedemptionOrderRepositoryImpl implements RedemptionOrderRepository {

    private final RedemptionOrderMapper orderMapper;

    public RedemptionOrderRepositoryImpl(RedemptionOrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public List<RedemptionOrder> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<RedemptionOrder>()
                .eq(RedemptionOrder::getUserId, userId)
                .orderByDesc(RedemptionOrder::getCreatedAt));
    }

    @Override
    public List<RedemptionOrder> findAllByOrderByCreatedAtDesc() {
        return orderMapper.selectList(new QueryWrapper<RedemptionOrder>()
                .orderByDesc("created_at"));
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return orderMapper.selectCount(new LambdaQueryWrapper<RedemptionOrder>()
                .eq(RedemptionOrder::getStatus, status));
    }

    @Override
    public long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end) {
        return orderMapper.selectCount(new LambdaQueryWrapper<RedemptionOrder>()
                .eq(RedemptionOrder::getStatus, status)
                .ge(RedemptionOrder::getCreatedAt, start)
                .lt(RedemptionOrder::getCreatedAt, end));
    }

    @Override
    public long countByStatusAndCompletedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end) {
        return orderMapper.selectCount(new LambdaQueryWrapper<RedemptionOrder>()
                .eq(RedemptionOrder::getStatus, status)
                .ge(RedemptionOrder::getCompletedAt, start)
                .lt(RedemptionOrder::getCompletedAt, end));
    }

    @Override
    public long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return orderMapper.selectCount(new LambdaQueryWrapper<RedemptionOrder>()
                .ge(RedemptionOrder::getCreatedAt, start)
                .lt(RedemptionOrder::getCreatedAt, end));
    }

    @Override
    public Optional<RedemptionOrder> findById(Long id) {
        return Optional.ofNullable(orderMapper.selectById(id));
    }

    @Override
    public RedemptionOrder save(RedemptionOrder order) {
        if (Objects.isNull(order.getId())) {
            orderMapper.insert(order);
        } else {
            orderMapper.updateById(order);
        }
        return order;
    }
}
