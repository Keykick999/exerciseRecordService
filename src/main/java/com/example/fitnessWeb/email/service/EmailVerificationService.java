package com.example.fitnessWeb.email.service;

import com.example.fitnessWeb.common.exception.DuplicateEmailException;
import com.example.fitnessWeb.common.exception.EmailNotVerifiedException;
import com.example.fitnessWeb.common.exception.InvalidVerificationCodeException;
import com.example.fitnessWeb.email.EmailVerificationPurpose;
import com.example.fitnessWeb.email.util.SmtpEmailSender;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redis;
    private final SmtpEmailSender mailSender;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${email.verification.code-length}")
    private int codeLength;

    @Value("${email.verification.code-ttl:5}")
    private long codeTtlMinutes;

    @Value("${email.verification.verified-ttl:15}")
    private long verifiedTtlMinutes;

    // ===================== 인증 코드 발송 =====================
    public void sendVerificationCode(String email, EmailVerificationPurpose purpose) {
        String code = generateVerificationCode();

        // Redis에 저장
        redis.opsForValue().set(verificationCodeKey(email, purpose), code, Duration.ofMinutes(codeTtlMinutes));

        // 이메일 전송
        String subject = "[FitLog] " + purpose + " 이메일 인증 코드";
        String body = "안녕하세요!\n\n요청하신 인증 코드는 " + code + " 입니다.\n" + codeTtlMinutes + "분 내에 입력해주세요.";

        mailSender.send(email, subject, body);
    }

    // ===================== 인증 코드 검증 =====================
    public void verifyAndMarkAsVerified(String email, EmailVerificationPurpose purpose, String code) {
        String saved = redis.opsForValue().get(verificationCodeKey(email, purpose));
        if (saved == null || !saved.equals(code)) {
            throw new InvalidVerificationCodeException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        // 사용 후 코드 삭제
        redis.delete(verificationCodeKey(email, purpose));

        // 인증 완료 마크 (TTL)
        redis.opsForValue().set(verificationMarkKey(email, purpose), "1", Duration.ofMinutes(verifiedTtlMinutes));
    }

    // ===================== 인증 상태 확인 =====================
    public void ensureVerified(String email, EmailVerificationPurpose purpose) {
        String key = verificationMarkKey(email, purpose);

        if (!Boolean.TRUE.equals(redis.hasKey(key))) {
            throw new EmailNotVerifiedException(email, purpose);
        }

        redis.delete(verificationMarkKey(email, purpose));
    }

    public void checkEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    // ===================== Redis Key 생성 =====================
    private String verificationCodeKey(String email, EmailVerificationPurpose purpose) {
        return "email:verification:code" + ":" + purpose + ":" + email;
    }

    private String verificationMarkKey(String email, EmailVerificationPurpose purpose) {
        return "email:verification:verified" + ":" + purpose + ":" + email;
    }

    // ===================== 인증 코드 생성 =====================
    private String generateVerificationCode() {
        int max = (int) Math.pow(10, codeLength);
        return String.format("%0" + codeLength + "d", random.nextInt(max));
    }
}
