package com.lowcarbon.platform.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lowcarbon.platform.entity.BehaviorReport;
import com.lowcarbon.platform.enums.ReportStatus;
import com.lowcarbon.platform.mapper.BehaviorReportMapper;
import com.lowcarbon.platform.repository.BehaviorReportRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class BehaviorReportRepositoryImpl implements BehaviorReportRepository {

    private final BehaviorReportMapper reportMapper;

    public BehaviorReportRepositoryImpl(BehaviorReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public List<BehaviorReport> findByStatusOrderBySubmittedAtAsc(ReportStatus status) {
        return reportMapper.selectList(new LambdaQueryWrapper<BehaviorReport>()
                .eq(BehaviorReport::getStatus, status)
                .orderByAsc(BehaviorReport::getSubmittedAt));
    }

    @Override
    public List<BehaviorReport> findByUserIdOrderBySubmittedAtDesc(Long userId) {
        return reportMapper.selectList(new LambdaQueryWrapper<BehaviorReport>()
                .eq(BehaviorReport::getUserId, userId)
                .orderByDesc(BehaviorReport::getSubmittedAt));
    }

    @Override
    public long countByStatus(ReportStatus status) {
        return reportMapper.selectCount(new LambdaQueryWrapper<BehaviorReport>()
                .eq(BehaviorReport::getStatus, status));
    }

    @Override
    public long countByUserIdAndRuleIdAndStatusAndSubmittedAtBetween(Long userId,
                                                                      Long ruleId,
                                                                      ReportStatus status,
                                                                      LocalDateTime start,
                                                                      LocalDateTime end) {
        return reportMapper.selectCount(new LambdaQueryWrapper<BehaviorReport>()
                .eq(BehaviorReport::getUserId, userId)
                .eq(BehaviorReport::getRuleId, ruleId)
                .eq(BehaviorReport::getStatus, status)
                .ge(BehaviorReport::getSubmittedAt, start)
                .lt(BehaviorReport::getSubmittedAt, end));
    }

    @Override
    public long countByUserIdAndRuleIdAndStatusInAndSubmittedAtBetween(Long userId,
                                                                        Long ruleId,
                                                                        List<ReportStatus> statuses,
                                                                        LocalDateTime start,
                                                                        LocalDateTime end) {
        return reportMapper.selectCount(new LambdaQueryWrapper<BehaviorReport>()
                .eq(BehaviorReport::getUserId, userId)
                .eq(BehaviorReport::getRuleId, ruleId)
                .in(BehaviorReport::getStatus, statuses)
                .ge(BehaviorReport::getSubmittedAt, start)
                .lt(BehaviorReport::getSubmittedAt, end));
    }

    @Override
    public long sumQuantityByUserIdAndRuleIdAndStatusInAndSubmittedAtBetween(Long userId,
                                                                              Long ruleId,
                                                                              List<ReportStatus> statuses,
                                                                              LocalDateTime start,
                                                                              LocalDateTime end) {
        Object value = reportMapper.selectObjs(new QueryWrapper<BehaviorReport>()
                .select("coalesce(sum(quantity), 0)")
                .eq("user_id", userId)
                .eq("rule_id", ruleId)
                .in("status", statuses.stream().map(Enum::name).toList())
                .ge("submitted_at", start)
                .lt("submitted_at", end))
                .stream()
                .findFirst()
                .orElse(0);
        return ((Number) value).longValue();
    }

    @Override
    public long countByStatusAndSubmittedAtBetween(ReportStatus status,
                                                   LocalDateTime start,
                                                   LocalDateTime end) {
        return reportMapper.selectCount(new LambdaQueryWrapper<BehaviorReport>()
                .eq(BehaviorReport::getStatus, status)
                .ge(BehaviorReport::getSubmittedAt, start)
                .lt(BehaviorReport::getSubmittedAt, end));
    }

    @Override
    public long countByStatusAndAuditedAtBetween(ReportStatus status,
                                                 LocalDateTime start,
                                                 LocalDateTime end) {
        return reportMapper.selectCount(new LambdaQueryWrapper<BehaviorReport>()
                .eq(BehaviorReport::getStatus, status)
                .ge(BehaviorReport::getAuditedAt, start)
                .lt(BehaviorReport::getAuditedAt, end));
    }

    @Override
    public List<BehaviorReport> findAll() {
        return reportMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public Optional<BehaviorReport> findById(Long id) {
        return Optional.ofNullable(reportMapper.selectById(id));
    }

    @Override
    public BehaviorReport save(BehaviorReport report) {
        if (Objects.isNull(report.getId())) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateById(report);
        }
        return report;
    }
}
