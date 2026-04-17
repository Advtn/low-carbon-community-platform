package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.BehaviorRule;

import java.util.List;
import java.util.Optional;

public interface BehaviorRuleRepository {

    long countByActiveTrue();

    List<BehaviorRule> findAll();

    Optional<BehaviorRule> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    BehaviorRule save(BehaviorRule rule);
}
