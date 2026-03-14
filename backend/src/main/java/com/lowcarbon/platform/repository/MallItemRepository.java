package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.MallItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MallItemRepository extends JpaRepository<MallItem, Long> {

    List<MallItem> findByEnabledTrueOrderByIdAsc();

    long countByEnabledTrue();

    @Query("select coalesce(sum(i.stock), 0) from MallItem i where i.enabled = true")
    long sumEnabledStock();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from MallItem i where i.id = :id")
    Optional<MallItem> findByIdForUpdate(@Param("id") Long id);
}
