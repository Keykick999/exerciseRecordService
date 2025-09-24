package com.example.fitnessWeb.common.exception;

public class DuplicateRoutineSetException extends RuntimeException {
    public DuplicateRoutineSetException(Long exerciseTypeId, int setNumber) {
        super("운동 ID " + exerciseTypeId + "에 대해 중복된 세트 번호 " + setNumber + "가 존재합니다.");
    }
}
