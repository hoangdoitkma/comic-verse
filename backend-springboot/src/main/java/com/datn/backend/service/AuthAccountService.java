package com.datn.backend.service;

import com.datn.backend.dto.request.ResetPasswordRequest;
import com.datn.backend.dto.response.JwtResponse;
import com.datn.backend.entity.PasswordResetToken;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.Role;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.repository.PasswordResetTokenRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.security.jwt.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthAccountService {

    private static final String PROVIDER_GOOGLE = "GOOGLE";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Value("${app.password-reset.otp-expiration-minutes:15}")
    private int otpExpirationMinutes;

    @Value("${app.password-reset.max-attempts:5}")
    private int maxAttempts;

    @Transactional
    public JwtResponse loginWithGoogle(String idTokenString) {
        GoogleIdToken.Payload payload = verifyGoogleIdToken(idTokenString);
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
        }

        String email = payload.getEmail();
        String providerId = payload.getSubject();
        String displayName = truncate((String) payload.get("name"), 26);
        String avatarUrl = (String) payload.get("picture");

        User user = userRepository.findByEmail(email)
                .map(existing -> updateGoogleAccount(existing, providerId, displayName, avatarUrl))
                .orElseGet(() -> createGoogleAccount(email, providerId, displayName, avatarUrl));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is banned");
        }

        return buildJwtResponse(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

            passwordResetTokenRepository.findByUserAndUsedFalse(user)
                    .forEach(token -> token.setUsed(true));

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .otpHash(passwordEncoder.encode(otp))
                    .expiresAt(expiresAt)
                    .used(false)
                    .attemptCount(0)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            try {
                emailService.sendPasswordResetOtp(user.getEmail(), user.getDisplayName(), otp, otpExpirationMinutes);
            } catch (MailException ex) {
                log.error("Failed to send password reset email to {}", user.getEmail(), ex);
            }
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset code"));

        PasswordResetToken token = passwordResetTokenRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset code"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setUsed(true);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset code");
        }

        if (token.getAttemptCount() >= maxAttempts) {
            token.setUsed(true);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many invalid attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(request.getOtp(), token.getOtpHash())) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            if (token.getAttemptCount() >= maxAttempts) {
                token.setUsed(true);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset code");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        if (!StringUtils.hasText(user.getAuthProvider())) {
            user.setAuthProvider("LOCAL");
        }
        token.setUsed(true);
        userRepository.save(user);
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
        if (!StringUtils.hasText(googleClientId)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google login is not configured");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token", ex);
        }
    }

    private User createGoogleAccount(String email, String providerId, String displayName, String avatarUrl) {
        User user = User.builder()
                .email(email)
                .displayName(StringUtils.hasText(displayName) ? displayName : email)
                .avatarUrl(avatarUrl)
                .authProvider(PROVIDER_GOOGLE)
                .providerId(providerId)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    private User updateGoogleAccount(User user, String providerId, String displayName, String avatarUrl) {
        if (!StringUtils.hasText(user.getAuthProvider())) {
            user.setAuthProvider(PROVIDER_GOOGLE);
        }
        if (!StringUtils.hasText(user.getProviderId())) {
            user.setProviderId(providerId);
        }
        if (StringUtils.hasText(displayName) && !StringUtils.hasText(user.getDisplayName())) {
            user.setDisplayName(displayName);
        }
        if (StringUtils.hasText(avatarUrl) && !StringUtils.hasText(user.getAvatarUrl())) {
            user.setAvatarUrl(avatarUrl);
        }
        return userRepository.save(user);
    }

    private JwtResponse buildJwtResponse(User user) {
        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
        List<String> roles = List.of("ROLE_" + user.getRole().name());
        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
