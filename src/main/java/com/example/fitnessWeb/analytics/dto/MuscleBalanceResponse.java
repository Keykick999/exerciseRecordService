package com.example.fitnessWeb.analytics.dto;
import java.math.BigDecimal;
import java.util.List;

public class MuscleBalanceResponse {
    private String username;   // ← userId 대신 username
    private String start;      // ISO yyyy-MM-dd
    private String end;        // ISO yyyy-MM-dd
    private Long totalVolume;  // 전체 볼륨 합
    private List<Item> byMuscle;

    public MuscleBalanceResponse(String username, String start, String end, Long totalVolume, List<Item> byMuscle) {
        this.username = username;
        this.start = start;
        this.end = end;
        this.totalVolume = totalVolume;
        this.byMuscle = byMuscle;
    }

    public static class Item {
        private String muscle;
        private Long volume;
        private Long totalVolume;
        private BigDecimal pct; // 0~100, 소수1자리

        public Item(String muscle, Long volume, Long totalVolume, BigDecimal pct) {
            this.muscle = muscle;
            this.volume = volume;
            this.totalVolume = totalVolume;
            this.pct = pct;
        }

        public String getMuscle() { return muscle; }
        public Long getVolume() { return volume; }
        public Long getTotalVolume() { return totalVolume; }
        public BigDecimal getPct() { return pct; }
    }

    public String getUsername() { return username; }
    public String getStart() { return start; }
    public String getEnd() { return end; }
    public Long getTotalVolume() { return totalVolume; }
    public List<Item> getByMuscle() { return byMuscle; }
}