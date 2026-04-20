package com.lowcarbon.platform.service;

import com.lowcarbon.platform.dto.LoginRequest;
import com.lowcarbon.platform.dto.LoginResponse;
import com.lowcarbon.platform.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    void logout(String authorizationHeader);
}
