package com.lowcarbon.platform.service;

import com.lowcarbon.platform.dto.LoginRequest;
import com.lowcarbon.platform.dto.LoginResponse;
import com.lowcarbon.platform.dto.RegisterCodeRequest;
import com.lowcarbon.platform.dto.RegisterCodeResponse;
import com.lowcarbon.platform.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterCodeResponse sendRegisterCode(RegisterCodeRequest request);

    LoginResponse register(RegisterRequest request);

    void logout(String authorizationHeader);
}
