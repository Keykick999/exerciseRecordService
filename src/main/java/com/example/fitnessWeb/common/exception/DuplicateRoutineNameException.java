package com.example.fitnessWeb.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)  // 409 상태 코드
public class DuplicateRoutineNameException extends RuntimeException {
    public DuplicateRoutineNameException(String routineName) {
        super("이미 존재하는 루틴 이름입니다: " + routineName);
    }
}
