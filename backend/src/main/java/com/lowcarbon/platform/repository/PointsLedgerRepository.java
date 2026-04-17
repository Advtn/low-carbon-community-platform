package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.PointsLedger;

import java.util.List;

public interface PointsLedgerRepository {

    List<PointsLedger> findByUserIdOrderByCreatedAtDesc(Long userId);

    PointsLedger save(PointsLedger ledger);
}
