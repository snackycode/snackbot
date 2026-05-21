package com.stattracker.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAccountDto {
    private String id;
    private String discordUserId;
    private String game;
    private String gameUsername;
    private String region;
    private Instant linkedAt;
    private boolean verified;
}
