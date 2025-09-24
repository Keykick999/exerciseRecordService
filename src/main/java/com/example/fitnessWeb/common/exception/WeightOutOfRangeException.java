package com.example.fitnessWeb.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WeightOutOfRangeException extends RuntimeException {
    public WeightOutOfRangeException(int weight) {
        super("weight는 0 이상이어야 합니다. 입력값=" + weight);
    }
}