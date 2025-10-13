package com.example.fitnessWeb.level.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelResponse {
    private int level;
    private int xp;

    public LevelResponse(int level, int xp) {
        this.level = level;
        this.xp = xp;
    }
}