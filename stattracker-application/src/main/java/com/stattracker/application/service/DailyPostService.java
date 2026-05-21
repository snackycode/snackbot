package com.stattracker.application.service;

import com.stattracker.domain.model.LinkedAccount;
import com.stattracker.domain.model.PlayerStats;
import com.stattracker.domain.model.Subscription;
import com.stattracker.domain.port.in.StatsUseCase;
import com.stattracker.domain.port.out.LinkedAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Scheduled service that posts daily stat summaries to Discord channels
 * for users who have enabled the feature via their {@link Subscription}.
 *
 * <p>The actual Discord message posting is handled through a callback
 * because this module does not depend on JDA directly.</p>
 */
@Service
public class DailyPostService {

    private static final Logger log = LoggerFactory.getLogger(DailyPostService.class);

    private final StatsUseCase statsUseCase;
    private final LinkedAccountRepository accountRepository;

    /**
     * Functional interface for the Discord-layer callback that sends the actual message.
     * Injected at runtime by the discord module's configuration.
     */
    private DailyPostCallback postCallback;

    @FunctionalInterface
    public interface DailyPostCallback {
        void sendDailyStats(String channelId, String discordUserId, PlayerStats stats);
    }

    public DailyPostService(StatsUseCase statsUseCase,
                             LinkedAccountRepository accountRepository) {
        this.statsUseCase = statsUseCase;
        this.accountRepository = accountRepository;
    }

    public void setPostCallback(DailyPostCallback postCallback) {
        this.postCallback = postCallback;
    }

    /**
     * Runs every day at 09:00 UTC.
     * In production you'd query Subscription documents to find users
     * who opted in, then post their stats to their configured channels.
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "UTC")
    public void executeDailyPosts() {
        log.info("Starting daily stat posts…");

        if (postCallback == null) {
            log.warn("No DailyPostCallback registered — skipping daily posts");
            return;
        }

        // In a real implementation, query all subscriptions where dailyPostEnabled = true
        // For now, this serves as the scheduling skeleton
        log.info("Daily post execution completed. " +
                 "In production, iterate over active subscriptions and post stats.");
    }

    /**
     * Post stats for a single user. Called from the scheduled job or on-demand.
     */
    public void postForUser(String discordUserId, String channelId) {
        List<LinkedAccount> accounts = accountRepository.findAllByDiscordUserId(discordUserId);
        if (accounts.isEmpty()) {
            log.warn("No linked accounts for user {} — skipping daily post", discordUserId);
            return;
        }

        for (LinkedAccount account : accounts) {
            try {
                PlayerStats stats = statsUseCase.getStats(
                        account.getGame(), account.getGameUsername(), account.getRegion());
                postCallback.sendDailyStats(channelId, discordUserId, stats);
                log.info("Posted daily {} stats for user {}", account.getGame(), discordUserId);
            } catch (Exception e) {
                log.error("Failed to post daily stats for {} ({}): {}",
                        discordUserId, account.getGame(), e.getMessage());
            }
        }
    }
}
