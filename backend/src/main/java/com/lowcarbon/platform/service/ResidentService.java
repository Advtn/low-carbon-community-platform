package com.lowcarbon.platform.service;

import com.lowcarbon.platform.dto.BehaviorReportCreateRequest;
import com.lowcarbon.platform.dto.RedeemRequest;
import com.lowcarbon.platform.dto.UserProfileUpdateRequest;

import java.util.List;
import java.util.Map;

public interface ResidentService {

    Map<String, Object> myProfile(Long userId);

    Map<String, Object> updateProfile(Long userId, UserProfileUpdateRequest request);

    List<Map<String, Object>> listRules();

    Map<String, Object> submitReport(Long userId, BehaviorReportCreateRequest request);

    List<Map<String, Object>> myReports(Long userId);

    List<Map<String, Object>> myLedger(Long userId);

    List<Map<String, Object>> listMallItems();

    Map<String, Object> redeem(Long userId, RedeemRequest request);

    List<Map<String, Object>> myOrders(Long userId);

    List<Map<String, Object>> leaderboard();
}
