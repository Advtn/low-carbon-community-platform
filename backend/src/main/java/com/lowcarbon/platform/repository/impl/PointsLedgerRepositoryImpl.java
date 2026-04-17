package com.lowcarbon.platform.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lowcarbon.platform.entity.PointsLedger;
import com.lowcarbon.platform.mapper.PointsLedgerMapper;
import com.lowcarbon.platform.repository.PointsLedgerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PointsLedgerRepositoryImpl implements PointsLedgerRepository {

    private final PointsLedgerMapper ledgerMapper;

    public PointsLedgerRepositoryImpl(PointsLedgerMapper ledgerMapper) {
        this.ledgerMapper = ledgerMapper;
    }

    @Override
    public List<PointsLedger> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return ledgerMapper.selectList(new LambdaQueryWrapper<PointsLedger>()
                .eq(PointsLedger::getUserId, userId)
                .orderByDesc(PointsLedger::getCreatedAt));
    }

    @Override
    public PointsLedger save(PointsLedger ledger) {
        if (Objects.isNull(ledger.getId())) {
            ledgerMapper.insert(ledger);
        } else {
            ledgerMapper.updateById(ledger);
        }
        return ledger;
    }
}
