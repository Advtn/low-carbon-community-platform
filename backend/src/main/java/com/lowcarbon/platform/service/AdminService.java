package com.lowcarbon.platform.service;

import com.lowcarbon.platform.dto.AuditRequest;
import com.lowcarbon.platform.dto.ItemRequest;
import com.lowcarbon.platform.dto.OrderStatusUpdateRequest;
import com.lowcarbon.platform.dto.RuleRequest;
import com.lowcarbon.platform.dto.UserUpsertRequest;

import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, Object> dashboard();

    List<Map<String, Object>> listUsers();

    Map<String, Object> createUser(UserUpsertRequest request);

    Map<String, Object> updateUser(Long userId, UserUpsertRequest request);

    void deleteUser(Long userId);

    List<Map<String, Object>> listRules();

    Map<String, Object> createRule(RuleRequest request);

    Map<String, Object> updateRule(Long id, RuleRequest request);

    void deleteRule(Long id);

    List<Map<String, Object>> listPendingReports();

    List<Map<String, Object>> listAllReports();

    Map<String, Object> auditReport(Long adminId, Long reportId, AuditRequest request);

    List<Map<String, Object>> listItems();

    Map<String, Object> createItem(ItemRequest request);

    Map<String, Object> updateItem(Long id, ItemRequest request);

    void deleteItem(Long id);

    List<Map<String, Object>> listOrders();

    Map<String, Object> updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
}
