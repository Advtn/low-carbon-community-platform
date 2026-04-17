package com.lowcarbon.platform.service.impl;

import com.lowcarbon.platform.dto.AuditRequest;
import com.lowcarbon.platform.dto.ItemRequest;
import com.lowcarbon.platform.dto.OrderStatusUpdateRequest;
import com.lowcarbon.platform.dto.RuleRequest;
import com.lowcarbon.platform.dto.UserUpsertRequest;
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
import com.lowcarbon.platform.service.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BehaviorRuleRepository ruleRepository;
    private final BehaviorReportRepository reportRepository;
    private final PointsLedgerRepository ledgerRepository;
    private final MallItemRepository itemRepository;
    private final RedemptionOrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository,
                            BehaviorRuleRepository ruleRepository,
                            BehaviorReportRepository reportRepository,
                            PointsLedgerRepository ledgerRepository,
                            MallItemRepository itemRepository,
                            RedemptionOrderRepository orderRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ruleRepository = ruleRepository;
        this.reportRepository = reportRepository;
        this.ledgerRepository = ledgerRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Map<String, Object> dashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Map<String, Object> data = new HashMap<>();
        data.put("residentCount", userRepository.countByRole(UserRole.RESIDENT));
        data.put("activeRuleCount", ruleRepository.countByActiveTrue());
        data.put("pendingReportCount", reportRepository.countByStatus(ReportStatus.PENDING));
        data.put("pendingOrderCount", orderRepository.countByStatus(OrderStatus.PENDING));
        data.put("enabledItemCount", itemRepository.countByEnabledTrue());
        data.put("enabledItemStock", itemRepository.sumEnabledStock());
        data.put("totalResidentPoints", userRepository.sumTotalPointsByRole(UserRole.RESIDENT));
        data.put("totalCarbonReduction", userRepository.sumTotalCarbonReductionByRole(UserRole.RESIDENT));
        data.put("todaySubmittedReports", reportRepository.countByStatusAndSubmittedAtBetween(ReportStatus.PENDING, start, end)
                + reportRepository.countByStatusAndSubmittedAtBetween(ReportStatus.APPROVED, start, end)
                + reportRepository.countByStatusAndSubmittedAtBetween(ReportStatus.REJECTED, start, end));
        data.put("todayApprovedReports", reportRepository.countByStatusAndAuditedAtBetween(ReportStatus.APPROVED, start, end));
        data.put("todayRedeemOrders", orderRepository.countByCreatedAtBetween(start, end));
        data.put("todayCompletedOrders", orderRepository.countByStatusAndCompletedAtBetween(OrderStatus.COMPLETED, start, end));
        data.put("updatedAt", LocalDateTime.now());
        return data;
    }

    @Override
    public List<Map<String, Object>> listUsers() {
        return userRepository.findAll().stream().map(this::toUserMap).toList();
    }

    @Transactional
    @Override
    public Map<String, Object> createUser(UserUpsertRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(u -> {
            throw new ApiException("用户名已存在");
        });

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(requirePassword(request.password())));
        user.setNickname(request.nickname());
        user.setRole(request.role());
        return toUserMap(userRepository.save(user));
    }

    @Transactional
    @Override
    public Map<String, Object> updateUser(Long userId, UserUpsertRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("用户不存在"));

        userRepository.findByUsername(request.username()).ifPresent(exist -> {
            if (!exist.getId().equals(userId)) {
                throw new ApiException("用户名已存在");
            }
        });

        user.setUsername(request.username());
        if (StringUtils.hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password().trim()));
        }
        user.setNickname(request.nickname());
        user.setRole(request.role());
        return toUserMap(userRepository.save(user));
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException("用户不存在");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<Map<String, Object>> listRules() {
        return ruleRepository.findAll().stream().map(this::toRuleMap).toList();
    }

    @Transactional
    @Override
    public Map<String, Object> createRule(RuleRequest request) {
        BehaviorRule rule = new BehaviorRule();
        applyRule(rule, request);
        return toRuleMap(ruleRepository.save(rule));
    }

    @Transactional
    @Override
    public Map<String, Object> updateRule(Long id, RuleRequest request) {
        BehaviorRule rule = ruleRepository.findById(id).orElseThrow(() -> new ApiException("规则不存在"));
        applyRule(rule, request);
        return toRuleMap(ruleRepository.save(rule));
    }

    @Transactional
    @Override
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ApiException("规则不存在");
        }
        ruleRepository.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listPendingReports() {
        return reportRepository.findByStatusOrderBySubmittedAtAsc(ReportStatus.PENDING).stream()
                .map(this::toReportMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> listAllReports() {
        return reportRepository.findAll().stream().map(this::toReportMap).toList();
    }

    @Transactional
    @Override
    public Map<String, Object> auditReport(Long adminId, Long reportId, AuditRequest request) {
        BehaviorReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException("上报记录不存在"));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ApiException("该上报记录已审核");
        }

        report.setAuditorId(adminId);
        report.setAuditedAt(LocalDateTime.now());
        report.setAuditRemark(request.remark());

        if (Boolean.TRUE.equals(request.approved())) {
            User user = userRepository.findByIdForUpdate(report.getUserId())
                    .orElseThrow(() -> new ApiException("用户不存在"));
            BehaviorRule rule = ruleRepository.findById(report.getRuleId())
                    .orElseThrow(() -> new ApiException("规则不存在"));
            int points = report.getQuantity() * rule.getPointsPerAction();
            double carbon = report.getQuantity() * rule.getCarbonReductionPerAction();

            user.setTotalPoints(user.getTotalPoints() + points);
            user.setTotalCarbonReduction(user.getTotalCarbonReduction() + carbon);

            report.setStatus(ReportStatus.APPROVED);
            report.setGrantedPoints(points);
            report.setGrantedCarbon(carbon);

            userRepository.save(user);

            PointsLedger ledger = new PointsLedger();
            ledger.setUserId(user.getId());
            ledger.setChangePoints(points);
            ledger.setType(LedgerType.REPORT_REWARD);
            ledger.setRelatedId(report.getId());
            ledger.setDescription("低碳行为奖励：" + rule.getName());
            ledger.setBalanceAfter(user.getTotalPoints());
            ledger.setCreatedAt(LocalDateTime.now());
            ledgerRepository.save(ledger);
        } else {
            report.setStatus(ReportStatus.REJECTED);
            report.setGrantedPoints(0);
            report.setGrantedCarbon(0.0);
        }

        return toReportMap(reportRepository.save(report));
    }

    @Override
    public List<Map<String, Object>> listItems() {
        return itemRepository.findAll().stream().map(this::toItemMap).toList();
    }

    @Transactional
    @Override
    public Map<String, Object> createItem(ItemRequest request) {
        MallItem item = new MallItem();
        applyItem(item, request);
        return toItemMap(itemRepository.save(item));
    }

    @Transactional
    @Override
    public Map<String, Object> updateItem(Long id, ItemRequest request) {
        MallItem item = itemRepository.findById(id).orElseThrow(() -> new ApiException("商品不存在"));
        applyItem(item, request);
        return toItemMap(itemRepository.save(item));
    }

    @Transactional
    @Override
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ApiException("商品不存在");
        }
        itemRepository.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toOrderMap).toList();
    }

    @Transactional
    @Override
    public Map<String, Object> updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        RedemptionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("订单不存在"));

        if (order.getStatus() == request.status()) {
            return toOrderMap(order);
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new ApiException("订单已结束，无法再次修改状态");
        }

        order.setStatus(request.status());
        order.setRemark(request.remark());

        if (request.status() == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        if (request.status() == OrderStatus.CANCELLED) {
            User user = userRepository.findByIdForUpdate(order.getUserId())
                    .orElseThrow(() -> new ApiException("用户不存在"));
            MallItem item = itemRepository.findByIdForUpdate(order.getItemId())
                    .orElseThrow(() -> new ApiException("商品不存在"));
            user.setTotalPoints(user.getTotalPoints() + order.getTotalPoints());
            item.setStock(item.getStock() + order.getQuantity());

            userRepository.save(user);
            itemRepository.save(item);

            PointsLedger refund = new PointsLedger();
            refund.setUserId(user.getId());
            refund.setChangePoints(order.getTotalPoints());
            refund.setType(LedgerType.REFUND);
            refund.setRelatedId(order.getId());
            refund.setDescription("订单取消退还积分：" + item.getName());
            refund.setBalanceAfter(user.getTotalPoints());
            refund.setCreatedAt(LocalDateTime.now());
            ledgerRepository.save(refund);
        }

        return toOrderMap(orderRepository.save(order));
    }

    private void applyRule(BehaviorRule rule, RuleRequest request) {
        rule.setName(request.name());
        rule.setDescription(request.description());
        rule.setPointsPerAction(request.pointsPerAction());
        rule.setCarbonReductionPerAction(request.carbonReductionPerAction());
        rule.setDailyLimit(request.dailyLimit());
        rule.setActive(request.active());
    }

    private void applyItem(MallItem item, ItemRequest request) {
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPointsCost(request.pointsCost());
        item.setStock(request.stock());
        item.setEnabled(request.enabled());
    }

    private String requirePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new ApiException("密码不能为空");
        }
        return password.trim();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("用户不存在"));
    }

    private BehaviorRule getRule(Long ruleId) {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ApiException("规则不存在"));
    }

    private MallItem getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("商品不存在"));
    }

    private Map<String, Object> toUserMap(User user) {
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
        data.put("auditorId", report.getAuditorId());
        data.put("auditRemark", report.getAuditRemark());
        data.put("grantedPoints", report.getGrantedPoints());
        data.put("grantedCarbon", report.getGrantedCarbon());
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
