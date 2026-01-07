package com.example.fitnessWeb.email.service;

import com.example.fitnessWeb.common.exception.DuplicateEmailException;
import com.example.fitnessWeb.common.exception.EmailNotVerifiedException;
import com.example.fitnessWeb.common.exception.InvalidVerificationCodeException;
import com.example.fitnessWeb.email.util.EmailSender;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final StringRedisTemplate redis;
    private final EmailSender mailSender;
    private final Random random = new Random();
    private final UserRepository userRepository;

    @Value("${email.verification.code-length}")
    private int codeLength;

    @Value("${email.verification.code-ttl:5}")
    private long codeTtlMinutes;

    @Value("${email.verification.verified-ttl:15}")
    private long verifiedTtlMinutes;

    // ===================== 인증 코드 발송 =====================
    @Override
    public void sendVerificationCode(String email, String purpose) {
        String code = generateCode();

        // Redis에 저장 (TTL)
        redis.opsForValue().set(codeKey(email, purpose), code, Duration.ofMinutes(codeTtlMinutes));

        // 이메일 전송
        String subject = "[FilterFacts] " + purpose + " 이메일 인증 코드";
        String text = "안녕하세요!\n\n요청하신 인증 코드는 " + code + " 입니다.\n" + codeTtlMinutes + "분 내에 입력해주세요.";

        mailSender.send(email, subject, text);

        System.out.println("[EMAIL] send code " + code + " to " + email + " for " + purpose);
    }

    // ===================== 인증 코드 검증 =====================
    @Override
    public void verifyAndMarkAsVerified(String email, String purpose, String code) {
        String saved = redis.opsForValue().get(codeKey(email, purpose));
        if (saved == null || !saved.equals(code)) {
            throw new InvalidVerificationCodeException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        // 사용 후 코드 삭제
        redis.delete(codeKey(email, purpose));

        // 인증 완료 마크 (TTL)
        redis.opsForValue().set(verifiedKey(email, purpose), "1", Duration.ofMinutes(verifiedTtlMinutes));
    }

    // ===================== 인증 상태 확인 =====================
    @Override
    public void ensureVerified(String email, String purpose) {
        ensureVerified(email, purpose, true); // 기본: 일회성 사용
    }

    public void ensureVerified(String email, String purpose, boolean oneTimeUse) {
        String v = redis.opsForValue().get(verifiedKey(email, purpose));
        if (v == null) {
            throw new EmailNotVerifiedException(email, purpose);
        }
        if (oneTimeUse) redis.delete(verifiedKey(email, purpose));
    }

    @Override
    public void checkEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            // 중복 시 예외 발생 (기존에 정의하신 DuplicateEmailException 사용)
            throw new DuplicateEmailException(email);
        }
    }

    // ===================== Redis Key 생성 =====================
    private String redisKey(String type, String email, String purpose) {
        return "ev:" + type + ":" + purpose + ":" + email;
    }

    private String codeKey(String email, String purpose) {
        return redisKey("code", email, purpose);
    }

    private String verifiedKey(String email, String purpose) {
        return redisKey("verified", email, purpose);
    }

    // ===================== 인증 코드 생성 =====================
    private String generateCode() {
        int max = (int) Math.pow(10, codeLength);
        return String.format("%0" + codeLength + "d", random.nextInt(max));
    }
}
