package com.stattracker.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatsDto {
    private String game;
    private String username;
    private String region;
    private int level;
    private long totalKills;
    private long totalDeaths;
    private double kdRatio;
    private double winRate;
    private String rankDisplay;
    private Map<String, String> extraMetrics;
}
