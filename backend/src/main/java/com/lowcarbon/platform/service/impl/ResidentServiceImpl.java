package com.lowcarbon.platform.service.impl;

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
import com.lowcarbon.platform.service.ResidentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResidentServiceImpl implements ResidentService {

    private final UserRepository userRepository;
    private final BehaviorRuleRepository ruleRepository;
    private final BehaviorReportRepository reportRepository;
    private final PointsLedgerRepository ledgerRepository;
    private final MallItemRepository itemRepository;
    private final RedemptionOrderRepository orderRepository;

    public ResidentServiceImpl(UserRepository userRepository,
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

    @Override
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

    @Override
    public List<Map<String, Object>> listRules() {
        return ruleRepository.findAll().stream()
                .filter(BehaviorRule::getActive)
                .map(this::toRuleMap)
                .toList();
    }

    @Transactional
    @Override
    public Map<String, Object> submitReport(Long userId, BehaviorReportCreateRequest request) {
        User user = getUser(userId);
        BehaviorRule rule = getRule(request.ruleId());

        if (!rule.getActive()) {
            throw new ApiException("该行为规则已停用");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<ReportStatus> statuses = Arrays.asList(ReportStatus.PENDING, ReportStatus.APPROVED);
        long usedQuantity = reportRepository.sumQuantityByUserIdAndRuleIdAndStatusInAndSubmittedAtBetween(
                user.getId(),
                rule.getId(),
                statuses,
                start,
                end
        );

        if (usedQuantity + request.quantity() > rule.getDailyLimit()) {
            long remain = Math.max(rule.getDailyLimit() - usedQuantity, 0);
            throw new ApiException("超过今日上报次数限制，今日剩余额度：" + remain);
        }

        BehaviorReport report = new BehaviorReport();
        report.setUserId(user.getId());
        report.setRuleId(rule.getId());
        report.setQuantity(request.quantity());
        report.setProofText(request.proofText());
        report.setProofImageUrl(request.proofImageUrl());
        report.setStatus(ReportStatus.PENDING);
        report.setSubmittedAt(LocalDateTime.now());

        BehaviorReport saved = reportRepository.save(report);
        return toReportMap(saved);
    }

    @Override
    public List<Map<String, Object>> myReports(Long userId) {
        return reportRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .map(this::toReportMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> myLedger(Long userId) {
        return ledgerRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toLedgerMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> listMallItems() {
        return itemRepository.findByEnabledTrueOrderByIdAsc().stream()
                .map(this::toItemMap)
                .toList();
    }

    @Transactional
    @Override
    public Map<String, Object> redeem(Long userId, RedeemRequest request) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApiException("用户不存在"));
        MallItem item = itemRepository.findByIdForUpdate(request.itemId())
                .orElseThrow(() -> new ApiException("商品不存在"));

        if (!item.getEnabled()) {
            throw new ApiException("商品已下架");
        }

        int needPoints = item.getPointsCost() * request.quantity();
        if (item.getStock() < request.quantity()) {
            throw new ApiException("库存不足");
        }

        if (user.getTotalPoints() < needPoints) {
            throw new ApiException("积分不足，当前积分：" + user.getTotalPoints());
        }

        item.setStock(item.getStock() - request.quantity());
        user.setTotalPoints(user.getTotalPoints() - needPoints);

        RedemptionOrder order = new RedemptionOrder();
        order.setUserId(user.getId());
        order.setItemId(item.getId());
        order.setQuantity(request.quantity());
        order.setTotalPoints(needPoints);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        PointsLedger ledger = new PointsLedger();
        ledger.setUserId(user.getId());
        ledger.setChangePoints(-needPoints);
        ledger.setType(LedgerType.REDEMPTION);
        ledger.setRelatedId(order.getId());
        ledger.setDescription("积分兑换商品：" + item.getName());
        ledger.setBalanceAfter(user.getTotalPoints());
        ledger.setCreatedAt(LocalDateTime.now());
        ledgerRepository.save(ledger);

        userRepository.save(user);
        itemRepository.save(item);

        return toOrderMap(order);
    }

    @Override
    public List<Map<String, Object>> myOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderMap)
                .toList();
    }

    @Override
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
                .orElseThrow(() -> new ApiException("用户不存在"));
    }

    private BehaviorRule getRule(Long ruleId) {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ApiException("行为规则不存在"));
    }

    private MallItem getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("商品不存在"));
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
        User user = getUser(report.getUserId());
        BehaviorRule rule = getRule(report.getRuleId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", report.getId());
        data.put("userId", report.getUserId());
        data.put("nickname", user.getNickname());
        data.put("ruleId", report.getRuleId());
        data.put("ruleName", rule.getName());
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
        User user = getUser(order.getUserId());
        MallItem item = getItem(order.getItemId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("userId", order.getUserId());
        data.put("nickname", user.getNickname());
        data.put("itemId", order.getItemId());
        data.put("itemName", item.getName());
        data.put("quantity", order.getQuantity());
        data.put("totalPoints", order.getTotalPoints());
        data.put("status", order.getStatus());
        data.put("createdAt", order.getCreatedAt());
        data.put("completedAt", order.getCompletedAt());
        data.put("remark", order.getRemark());
        return data;
    }
}
