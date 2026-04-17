package com.lowcarbon.platform.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lowcarbon.platform.entity.BehaviorRule;
import com.lowcarbon.platform.mapper.BehaviorRuleMapper;
import com.lowcarbon.platform.repository.BehaviorRuleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class BehaviorRuleRepositoryImpl implements BehaviorRuleRepository {

    private final BehaviorRuleMapper ruleMapper;

    public BehaviorRuleRepositoryImpl(BehaviorRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    @Override
    public long countByActiveTrue() {
        return ruleMapper.selectCount(new LambdaQueryWrapper<BehaviorRule>().eq(BehaviorRule::getActive, true));
    }

    @Override
    public List<BehaviorRule> findAll() {
        return ruleMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public Optional<BehaviorRule> findById(Long id) {
        return Optional.ofNullable(ruleMapper.selectById(id));
    }

    @Override
    public boolean existsById(Long id) {
        return ruleMapper.selectCount(new LambdaQueryWrapper<BehaviorRule>().eq(BehaviorRule::getId, id)) > 0;
    }

    @Override
    public void deleteById(Long id) {
        ruleMapper.deleteById(id);
    }

    @Override
    public BehaviorRule save(BehaviorRule rule) {
        if (Objects.isNull(rule.getId())) {
            ruleMapper.insert(rule);
        } else {
            ruleMapper.updateById(rule);
        }
        return rule;
    }
}
