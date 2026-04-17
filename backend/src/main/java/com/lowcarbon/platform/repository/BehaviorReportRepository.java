package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.BehaviorReport;
import com.lowcarbon.platform.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BehaviorReportRepository {

    List<BehaviorReport> findByStatusOrderBySubmittedAtAsc(ReportStatus status);

    List<BehaviorReport> findByUserIdOrderBySubmittedAtDesc(Long userId);

    long countByStatus(ReportStatus status);

    long countByUserIdAndRuleIdAndStatusAndSubmittedAtBetween(Long userId,
                                                              Long ruleId,
                                                              ReportStatus status,
                                                              LocalDateTime start,
                                                              LocalDateTime end);

    long countByUserIdAndRuleIdAndStatusInAndSubmittedAtBetween(Long userId,
                                                                Long ruleId,
                                                                List<ReportStatus> statuses,
                                                                LocalDateTime start,
                                                                LocalDateTime end);

    long sumQuantityByUserIdAndRuleIdAndStatusInAndSubmittedAtBetween(Long userId,
                                                                      Long ruleId,
                                                                      List<ReportStatus> statuses,
                                                                      LocalDateTime start,
                                                                      LocalDateTime end);

    long countByStatusAndSubmittedAtBetween(ReportStatus status,
                                            LocalDateTime start,
                                            LocalDateTime end);

    long countByStatusAndAuditedAtBetween(ReportStatus status,
                                          LocalDateTime start,
                                          LocalDateTime end);

    List<BehaviorReport> findAll();

    Optional<BehaviorReport> findById(Long id);

    BehaviorReport save(BehaviorReport report);
}
