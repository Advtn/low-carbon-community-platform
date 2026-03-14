package com.lowcarbon.platform.controller;

import com.lowcarbon.platform.dto.AuditRequest;
import com.lowcarbon.platform.dto.ItemRequest;
import com.lowcarbon.platform.dto.OrderStatusUpdateRequest;
import com.lowcarbon.platform.dto.RuleRequest;
import com.lowcarbon.platform.dto.UserUpsertRequest;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.exception.ApiException;
import com.lowcarbon.platform.security.AuthContext;
import com.lowcarbon.platform.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return adminService.listUsers();
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@Valid @RequestBody UserUpsertRequest request) {
        return adminService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpsertRequest request) {
        return adminService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }

    @GetMapping("/rules")
    public List<Map<String, Object>> listRules() {
        return adminService.listRules();
    }

    @PostMapping("/rules")
    public Map<String, Object> createRule(@Valid @RequestBody RuleRequest request) {
        return adminService.createRule(request);
    }

    @PutMapping("/rules/{id}")
    public Map<String, Object> updateRule(@PathVariable Long id, @Valid @RequestBody RuleRequest request) {
        return adminService.updateRule(id, request);
    }

    @DeleteMapping("/rules/{id}")
    public void deleteRule(@PathVariable Long id) {
        adminService.deleteRule(id);
    }

    @GetMapping("/reports/pending")
    public List<Map<String, Object>> pendingReports() {
        return adminService.listPendingReports();
    }

    @GetMapping("/reports")
    public List<Map<String, Object>> allReports() {
        return adminService.listAllReports();
    }

    @PostMapping("/reports/{id}/audit")
    public Map<String, Object> audit(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        return adminService.auditReport(currentUserId(), id, request);
    }

    @GetMapping("/items")
    public List<Map<String, Object>> listItems() {
        return adminService.listItems();
    }

    @PostMapping("/items")
    public Map<String, Object> createItem(@Valid @RequestBody ItemRequest request) {
        return adminService.createItem(request);
    }

    @PutMapping("/items/{id}")
    public Map<String, Object> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return adminService.updateItem(id, request);
    }

    @DeleteMapping("/items/{id}")
    public void deleteItem(@PathVariable Long id) {
        adminService.deleteItem(id);
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> listOrders() {
        return adminService.listOrders();
    }

    @PostMapping("/orders/{id}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Long id,
                                                  @Valid @RequestBody OrderStatusUpdateRequest request) {
        return adminService.updateOrderStatus(id, request);
    }

    private Long currentUserId() {
        User user = AuthContext.get();
        if (user == null) {
            throw new ApiException("\u672a\u767b\u5f55");
        }
        return user.getId();
    }
}
