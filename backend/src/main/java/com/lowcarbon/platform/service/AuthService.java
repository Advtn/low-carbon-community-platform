package com.lowcarbon.platform.service;

import com.lowcarbon.platform.dto.LoginRequest;
import com.lowcarbon.platform.dto.LoginResponse;
import com.lowcarbon.platform.dto.RegisterRequest;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.UserRole;
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
                .orElseThrow(() -> new ApiException("用户名或密码错误"));

        if (!user.getPassword().equals(request.password())) {
            throw new ApiException("用户名或密码错误");
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

    public LoginResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String nickname = request.nickname().trim();
        String password = request.password().trim();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ApiException("用户名已存在，请更换后重试");
        }

        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword(password);
        user.setRole(UserRole.RESIDENT);
        user.setTotalPoints(0);
        user.setTotalCarbonReduction(0.0);

        User saved = userRepository.save(user);
        String token = tokenService.createToken(saved.getId());
        return new LoginResponse(
                token,
                saved.getId(),
                saved.getUsername(),
                saved.getNickname(),
                saved.getRole(),
                saved.getTotalPoints(),
                saved.getTotalCarbonReduction()
        );
    }
}

