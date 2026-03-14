package com.lowcarbon.platform.service;

import com.lowcarbon.platform.dto.BehaviorReportCreateRequest;
import com.lowcarbon.platform.dto.RedeemRequest;
import com.lowcarbon.platform.entity.BehaviorReport;
import com.lowcarbon.platform.entity.BehaviorRule;
import com.lowcarbon.platform.entity.MallItem;
import com.lowcarbon.platform.entity.PointsLedger;
import com.lowcarbon.platform.entity.RedemptionOrder;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.LedgerType;
import com.lowcarbon.platform.enums.OrderStatus;
import com.lowcarbon.platform.enums.ReportStatus;
import com.lowcarbon.platform.enums.UserRole;
import com.lowcarbon.platform.exception.ApiException;
import com.lowcarbon.platform.repository.BehaviorReportRepository;
import com.lowcarbon.platform.repository.BehaviorRuleRepository;
import com.lowcarbon.platform.repository.MallItemRepository;
import com.lowcarbon.platform.repository.PointsLedgerRepository;
import com.lowcarbon.platform.repository.RedemptionOrderRepository;
import com.lowcarbon.platform.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResidentService {

    private final UserRepository userRepository;
    private final BehaviorRuleRepository ruleRepository;
    private final BehaviorReportRepository reportRepository;
    private final PointsLedgerRepository ledgerRepository;
    private final MallItemRepository itemRepository;
    private final RedemptionOrderRepository orderRepository;

    public ResidentService(UserRepository userRepository,
                           BehaviorRuleRepository ruleRepository,
                           BehaviorReportRepository reportRepository,
                           PointsLedgerRepository ledgerRepository,
                           MallItemRepository itemRepository,
                           RedemptionOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.ruleRepository = ruleRepository;
        this.reportRepository = reportRepository;
        this.ledgerRepository = ledgerRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    public Map<String, Object> myProfile(Long userId) {
        User user = getUser(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        data.put("totalPoints", user.getTotalPoints());
        data.put("totalCarbonReduction", user.getTotalCarbonReduction());
        data.put("createdAt", user.getCreatedAt());
        return data;
    }

    public List<Map<String, Object>> listRules() {
        return ruleRepository.findAll().stream()
                .filter(BehaviorRule::getActive)
                .map(this::toRuleMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> submitReport(Long userId, BehaviorReportCreateRequest request) {
        User user = getUser(userId);
        BehaviorRule rule = ruleRepository.findById(request.ruleId())
                .orElseThrow(() -> new ApiException("\u884c\u4e3a\u89c4\u5219\u4e0d\u5b58\u5728"));

        if (!rule.getActive()) {
            throw new ApiException("\u8be5\u884c\u4e3a\u89c4\u5219\u5df2\u505c\u7528");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<ReportStatus> statuses = Arrays.asList(ReportStatus.PENDING, ReportStatus.APPROVED);
        long usedQuantity = reportRepository.sumQuantityByUserIdAndRuleIdAndStatusInAndSubmittedAtBetween(
                userId,
                rule.getId(),
                statuses,
                start,
                end
        );

        if (usedQuantity + request.quantity() > rule.getDailyLimit()) {
            long remain = Math.max(rule.getDailyLimit() - usedQuantity, 0);
            throw new ApiException("\u8d85\u8fc7\u4eca\u65e5\u4e0a\u62a5\u6b21\u6570\u9650\u5236\uff0c\u4eca\u65e5\u5269\u4f59\u989d\u5ea6\uff1a" + remain);
        }

        BehaviorReport report = new BehaviorReport();
        report.setUser(user);
        report.setRule(rule);
        report.setQuantity(request.quantity());
        report.setProofText(request.proofText());
        report.setProofImageUrl(request.proofImageUrl());
        report.setStatus(ReportStatus.PENDING);
        report.setSubmittedAt(LocalDateTime.now());

        BehaviorReport saved = reportRepository.save(report);
        return toReportMap(saved);
    }

    public List<Map<String, Object>> myReports(Long userId) {
        return reportRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .map(this::toReportMap)
                .toList();
    }

    public List<Map<String, Object>> myLedger(Long userId) {
        return ledgerRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toLedgerMap)
                .toList();
    }

    public List<Map<String, Object>> listMallItems() {
        return itemRepository.findByEnabledTrueOrderByIdAsc().stream()
                .map(this::toItemMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> redeem(Long userId, RedeemRequest request) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException("\u7528\u6237\u4e0d\u5b58\u5728"));
        MallItem item = itemRepository.findByIdForUpdate(request.itemId())
                .orElseThrow(() -> new ApiException("\u5546\u54c1\u4e0d\u5b58\u5728"));

        if (!item.getEnabled()) {
            throw new ApiException("\u5546\u54c1\u5df2\u4e0b\u67b6");
        }

        int needPoints = item.getPointsCost() * request.quantity();
        if (item.getStock() < request.quantity()) {
            throw new ApiException("\u5e93\u5b58\u4e0d\u8db3");
        }

        if (user.getTotalPoints() < needPoints) {
            throw new ApiException("\u79ef\u5206\u4e0d\u8db3\uff0c\u5f53\u524d\u79ef\u5206\uff1a" + user.getTotalPoints());
        }

        item.setStock(item.getStock() - request.quantity());
        user.setTotalPoints(user.getTotalPoints() - needPoints);

        RedemptionOrder order = new RedemptionOrder();
        order.setUser(user);
        order.setItem(item);
        order.setQuantity(request.quantity());
        order.setTotalPoints(needPoints);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        PointsLedger ledger = new PointsLedger();
        ledger.setUser(user);
        ledger.setChangePoints(-needPoints);
        ledger.setType(LedgerType.REDEMPTION);
        ledger.setRelatedId(order.getId());
        ledger.setDescription("\u79ef\u5206\u5151\u6362\u5546\u54c1\uff1a" + item.getName());
        ledger.setBalanceAfter(user.getTotalPoints());
        ledger.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(ledger);

        userRepository.save(user);
        itemRepository.save(item);

        return toOrderMap(order);
    }

    public List<Map<String, Object>> myOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderMap)
                .toList();
    }

    public List<Map<String, Object>> leaderboard() {
        return userRepository.findTop10ByRoleOrderByTotalPointsDescTotalCarbonReductionDesc(UserRole.RESIDENT).stream()
                .map(user -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", user.getId());
                    data.put("nickname", user.getNickname());
                    data.put("totalCarbonReduction", user.getTotalCarbonReduction());
                    data.put("totalPoints", user.getTotalPoints());
                    return data;
                })
                .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("\u7528\u6237\u4e0d\u5b58\u5728"));
    }

    private Map<String, Object> toRuleMap(BehaviorRule rule) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", rule.getId());
        data.put("name", rule.getName());
        data.put("description", rule.getDescription());
        data.put("pointsPerAction", rule.getPointsPerAction());
        data.put("carbonReductionPerAction", rule.getCarbonReductionPerAction());
        data.put("dailyLimit", rule.getDailyLimit());
        data.put("active", rule.getActive());
        return data;
    }

    private Map<String, Object> toReportMap(BehaviorReport report) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", report.getId());
        data.put("userId", report.getUser().getId());
        data.put("nickname", report.getUser().getNickname());
        data.put("ruleId", report.getRule().getId());
        data.put("ruleName", report.getRule().getName());
        data.put("quantity", report.getQuantity());
        data.put("proofText", report.getProofText());
        data.put("proofImageUrl", report.getProofImageUrl());
        data.put("status", report.getStatus());
        data.put("submittedAt", report.getSubmittedAt());
        data.put("auditedAt", report.getAuditedAt());
        data.put("auditRemark", report.getAuditRemark());
        data.put("grantedPoints", report.getGrantedPoints());
        data.put("grantedCarbon", report.getGrantedCarbon());
        return data;
    }

    private Map<String, Object> toLedgerMap(PointsLedger ledger) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", ledger.getId());
        data.put("changePoints", ledger.getChangePoints());
        data.put("type", ledger.getType());
        data.put("relatedId", ledger.getRelatedId());
        data.put("description", ledger.getDescription());
        data.put("balanceAfter", ledger.getBalanceAfter());
        data.put("createdAt", ledger.getCreatedAt());
        return data;
    }

    private Map<String, Object> toItemMap(MallItem item) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", item.getId());
        data.put("name", item.getName());
        data.put("description", item.getDescription());
        data.put("pointsCost", item.getPointsCost());
        data.put("stock", item.getStock());
        data.put("enabled", item.getEnabled());
        return data;
    }

    private Map<String, Object> toOrderMap(RedemptionOrder order) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("userId", order.getUser().getId());
        data.put("nickname", order.getUser().getNickname());
        data.put("itemId", order.getItem().getId());
        data.put("itemName", order.getItem().getName());
        data.put("quantity", order.getQuantity());
        data.put("totalPoints", order.getTotalPoints());
        data.put("status", order.getStatus());
        data.put("createdAt", order.getCreatedAt());
        data.put("completedAt", order.getCompletedAt());
        data.put("remark", order.getRemark());
        return data;
    }
}
