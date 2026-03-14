package com.lowcarbon.platform.service;

import com.lowcarbon.platform.dto.LoginRequest;
import com.lowcarbon.platform.dto.LoginResponse;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.exception.ApiException;
import com.lowcarbon.platform.repository.UserRepository;
import com.lowcarbon.platform.security.TokenService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException("閻劍鍩涢崥宥嗗灗鐎靛棛鐖滈柨娆掝嚖"));

        if (!user.getPassword().equals(request.password())) {
            throw new ApiException("閻劍鍩涢崥宥嗗灗鐎靛棛鐖滈柨娆掝嚖");
        }

        String token = tokenService.createToken(user.getId());
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getTotalPoints(),
                user.getTotalCarbonReduction()
        );
    }
}
