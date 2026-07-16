package com.example.fitnessWeb.exerciseRecord.service;

public record SaveExerciseSetCommand (
    Long exerciseTypeId,
    int setOrder,
    double weight,
    int reps
) {}