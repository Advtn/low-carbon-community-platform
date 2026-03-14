package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.BehaviorRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BehaviorRuleRepository extends JpaRepository<BehaviorRule, Long> {

    long countByActiveTrue();
}
