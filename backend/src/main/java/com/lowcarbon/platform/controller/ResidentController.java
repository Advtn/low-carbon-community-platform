package com.lowcarbon.platform.controller;

import com.lowcarbon.platform.dto.BehaviorReportCreateRequest;
import com.lowcarbon.platform.dto.RedeemRequest;
import com.lowcarbon.platform.dto.UserProfileUpdateRequest;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.exception.ApiException;
import com.lowcarbon.platform.security.AuthContext;
import com.lowcarbon.platform.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @GetMapping("/profile")
    public Map<String, Object> profile() {
        return residentService.myProfile(currentUserId());
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return residentService.updateProfile(currentUserId(), request);
    }

    @GetMapping("/rules")
    public List<Map<String, Object>> rules() {
        return residentService.listRules();
    }

    @PostMapping("/reports")
    public Map<String, Object> submitReport(@Valid @RequestBody BehaviorReportCreateRequest request) {
        return residentService.submitReport(currentUserId(), request);
    }

    @GetMapping("/reports")
    public List<Map<String, Object>> myReports() {
        return residentService.myReports(currentUserId());
    }

    @GetMapping("/ledger")
    public List<Map<String, Object>> ledger() {
        return residentService.myLedger(currentUserId());
    }

    @GetMapping("/mall/items")
    public List<Map<String, Object>> mallItems() {
        return residentService.listMallItems();
    }

    @PostMapping("/mall/redeem")
    public Map<String, Object> redeem(@Valid @RequestBody RedeemRequest request) {
        return residentService.redeem(currentUserId(), request);
    }

    @GetMapping("/mall/orders")
    public List<Map<String, Object>> myOrders() {
        return residentService.myOrders(currentUserId());
    }

    @GetMapping("/leaderboard")
    public List<Map<String, Object>> leaderboard() {
        return residentService.leaderboard();
    }

    private Long currentUserId() {
        User user = AuthContext.get();
        if (user == null) {
            throw new ApiException("\u672a\u767b\u5f55");
        }
        return user.getId();
    }
}
