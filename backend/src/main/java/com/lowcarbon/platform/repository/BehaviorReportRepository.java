package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.BehaviorReport;
import com.lowcarbon.platform.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BehaviorReportRepository extends JpaRepository<BehaviorReport, Long> {

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

    @Query("""
            select coalesce(sum(r.quantity), 0)
            from BehaviorReport r
            where r.user.id = :userId
              and r.rule.id = :ruleId
              and r.status in :statuses
              and r.submittedAt >= :start
              and r.submittedAt < :end
            """)
    long sumQuantityByUserIdAndRuleIdAndStatusInAndSubmittedAtBetween(@Param("userId") Long userId,
                                                                       @Param("ruleId") Long ruleId,
                                                                       @Param("statuses") List<ReportStatus> statuses,
                                                                       @Param("start") LocalDateTime start,
                                                                       @Param("end") LocalDateTime end);

    long countByStatusAndSubmittedAtBetween(ReportStatus status,
                                            LocalDateTime start,
                                            LocalDateTime end);

    long countByStatusAndAuditedAtBetween(ReportStatus status,
                                          LocalDateTime start,
                                          LocalDateTime end);
}
