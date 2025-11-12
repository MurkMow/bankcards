package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder, @Nullable RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body("Имя пользователя и пароль обязательны");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail() != null ? req.getEmail() : req.getUsername() + "@example.com");
        user.setRole(com.example.bankcards.entity.Role.USER);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            String accessToken = jwtUtil.generateAccessToken(req.getUsername());

            if (refreshTokenService != null) {
                User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
                return ResponseEntity.ok(Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken.getToken()
                ));
            } else {
                return ResponseEntity.ok(Map.of("accessToken", accessToken));
            }

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверные учетные данные");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        if (refreshTokenService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Обновление токена не поддерживается");
        }

        String token = body.get("refreshToken");
        if (token == null) return ResponseEntity.badRequest().body("refreshToken обязателен");

        Optional<RefreshToken> maybeToken = refreshTokenService.findByToken(token);
        if (maybeToken.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Токен не найден");

        RefreshToken rt = maybeToken.get();
        if (refreshTokenService.isExpired(rt)) {
            refreshTokenService.delete(rt);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Токен просрочен");
        }

        User user = rt.getUser();
        refreshTokenService.delete(rt);
        RefreshToken newRt = refreshTokenService.createRefreshToken(user);
        String newAccess = jwtUtil.generateAccessToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess,
                "refreshToken", newRt.getToken()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication auth) {
        if (auth != null && refreshTokenService != null) {
            userRepository.findByUsername(auth.getName()).ifPresent(refreshTokenService::deleteByUser);
        }
        return ResponseEntity.ok().build();
    }
}
