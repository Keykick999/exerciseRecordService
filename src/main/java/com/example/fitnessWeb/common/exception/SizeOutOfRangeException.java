package com.example.fitnessWeb.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 잘못된 요청 파라미터나 비즈니스 규칙 위반 시 던지는 예외
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SizeOutOfRangeException  extends RuntimeException {
    public SizeOutOfRangeException (String message) {
        super(message);
    }
}
