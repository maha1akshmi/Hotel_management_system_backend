package com.example.hms.service;

import com.example.hms.dto.*;
import com.example.hms.entity.User;
import com.example.hms.enums.Role;
import com.example.hms.exception.BadRequestException;
import com.example.hms.exception.UnauthorizedException;
import com.example.hms.repository.UserRepository;
import com.example.hms.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered. Please use a different email.");
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.USER)
                .isVerified(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails, savedUser.getId(), savedUser.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(mapToUserResponse(savedUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Contact support.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails, user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            String email = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
                throw new UnauthorizedException("Invalid or expired refresh token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            String newAccessToken = jwtUtil.generateAccessToken(userDetails, user.getId(), user.getRole().name());
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .user(mapToUserResponse(user))
                    .build();

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthResponse googleLogin(GoogleTokenRequest request) {
        // 1. Verify the Google ID token with Google's tokeninfo endpoint
        String idToken = request.getCredential();
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> tokenInfo;
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            tokenInfo = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Google token. Please try again.");
        }

        if (tokenInfo == null) {
            throw new UnauthorizedException("Could not verify Google token.");
        }

        // 2. Validate the audience matches our client ID
        String audience = (String) tokenInfo.get("aud");
        if (!googleClientId.equals(audience)) {
            throw new UnauthorizedException("Google token was not issued for this application.");
        }

        // 3. Extract user info from the verified token
        String googleId = (String) tokenInfo.get("sub");
        String email = (String) tokenInfo.get("email");
        String name = (String) tokenInfo.get("name");

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Google account does not have an email address.");
        }

        // 4. Find or create the user
        User user = userRepository.findByOauthProviderAndOauthId("GOOGLE", googleId)
                .orElseGet(() -> {
                    // Check if a user already exists with this email (registered via email/password)
                    return userRepository.findByEmail(email)
                            .map(existingUser -> {
                                // Link Google OAuth to the existing account
                                existingUser.setOauthProvider("GOOGLE");
                                existingUser.setOauthId(googleId);
                                return userRepository.save(existingUser);
                            })
                            .orElseGet(() -> {
                                // Create a brand-new user from Google sign-in
                                User newUser = User.builder()
                                        .name(name != null ? name : email.split("@")[0])
                                        .email(email)
                                        .oauthProvider("GOOGLE")
                                        .oauthId(googleId)
                                        .role(Role.USER)
                                        .isVerified(true) // Google-verified email
                                        .isActive(true)
                                        .build();
                                return userRepository.save(newUser);
                            });
                });

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Contact support.");
        }

        // 5. Generate JWT tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails, user.getId(), user.getRole().name());
        String refreshTokenStr = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .isVerified(user.getIsVerified())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

