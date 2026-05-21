package com.stattracker.api.controller;

import com.stattracker.api.dto.LinkedAccountDto;
import com.stattracker.api.dto.PlayerStatsDto;
import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.LinkedAccount;
import com.stattracker.domain.model.PlayerStats;
import com.stattracker.domain.port.in.LinkAccountUseCase;
import com.stattracker.domain.port.in.StatsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final StatsUseCase statsUseCase;
    private final LinkAccountUseCase linkAccountUseCase;

    public DashboardController(StatsUseCase statsUseCase, LinkAccountUseCase linkAccountUseCase) {
        this.statsUseCase = statsUseCase;
        this.linkAccountUseCase = linkAccountUseCase;
    }

    /**
     * Fetch stats directly for a specific game and player. (Public endpoint)
     */
    @GetMapping("/public/stats/{game}/{username}")
    public ResponseEntity<PlayerStatsDto> getPlayerStats(
            @PathVariable String game,
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "") String region) {

        Game parsedGame = Game.fromString(game);
        String resolvedRegion = region.isBlank() ? parsedGame.getValidRegions()[0] : region;

        PlayerStats stats = statsUseCase.getStats(parsedGame, username, resolvedRegion);

        PlayerStatsDto dto = PlayerStatsDto.builder()
                .game(stats.game().name())
                .username(stats.username())
                .region(stats.region())
                .level(stats.level())
                .totalKills(stats.totalKills())
                .totalDeaths(stats.totalDeaths())
                .kdRatio(stats.kdRatio())
                .winRate(stats.winRate())
                .rankDisplay(stats.rankedInfo() != null ? stats.rankedInfo().displayString() : "Unranked")
                .extraMetrics(stats.extra())
                .build();

        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieve all linked game accounts for a Discord user ID. (Secured dashboard endpoint)
     */
    @GetMapping("/dashboard/users/{discordUserId}/accounts")
    public ResponseEntity<List<LinkedAccountDto>> getUserAccounts(@PathVariable String discordUserId) {
        List<LinkedAccount> accounts = linkAccountUseCase.getLinkedAccounts(discordUserId);

        List<LinkedAccountDto> response = accounts.stream().map(acc ->
                LinkedAccountDto.builder()
                        .id(acc.getId())
                        .discordUserId(acc.getDiscordUserId())
                        .game(acc.getGame().name())
                        .gameUsername(acc.getGameUsername())
                        .region(acc.getRegion())
                        .linkedAt(acc.getLinkedAt())
                        .verified(acc.isVerified())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
