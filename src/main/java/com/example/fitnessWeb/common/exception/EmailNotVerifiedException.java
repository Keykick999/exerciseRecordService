package com.example.fitnessWeb.common.exception;

import com.example.fitnessWeb.email.EmailVerificationPurpose;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String email, EmailVerificationPurpose purpose) {
        super("이메일 미인증: email=" + email + ", purpose=" + purpose);
    }
}