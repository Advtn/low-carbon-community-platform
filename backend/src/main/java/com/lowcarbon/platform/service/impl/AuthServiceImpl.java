package com.lowcarbon.platform.service.impl;

import com.lowcarbon.platform.dto.LoginRequest;
import com.lowcarbon.platform.dto.LoginResponse;
import com.lowcarbon.platform.dto.RegisterRequest;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.UserRole;
import com.lowcarbon.platform.exception.ApiException;
import com.lowcarbon.platform.repository.UserRepository;
import com.lowcarbon.platform.security.TokenService;
import com.lowcarbon.platform.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           TokenService tokenService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef");
        }

        String token = tokenService.createToken(user.getId());
        return toLoginResponse(user, token);
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String nickname = request.nickname().trim();
        String password = request.password().trim();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ApiException("\u7528\u6237\u540d\u5df2\u5b58\u5728\uff0c\u8bf7\u66f4\u6362\u540e\u91cd\u8bd5");
        }

        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.RESIDENT);
        user.setTotalPoints(0);
        user.setTotalCarbonReduction(0.0);

        User saved = userRepository.save(user);
        String token = tokenService.createToken(saved.getId());
        return toLoginResponse(saved, token);
    }

    @Override
    public void logout(String authorizationHeader) {
        String token = tokenService.extractBearerToken(authorizationHeader);
        if (!StringUtils.hasText(token)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "\u672a\u767b\u5f55\u6216\u4ee4\u724c\u65e0\u6548");
        }
        tokenService.invalidate(token);
    }

    private LoginResponse toLoginResponse(User user, String token) {
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
