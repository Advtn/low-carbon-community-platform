package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.MallItem;

import java.util.List;
import java.util.Optional;

public interface MallItemRepository {

    List<MallItem> findByEnabledTrueOrderByIdAsc();

    long countByEnabledTrue();

    long sumEnabledStock();

    Optional<MallItem> findByIdForUpdate(Long id);

    List<MallItem> findAll();

    Optional<MallItem> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    MallItem save(MallItem item);
}
