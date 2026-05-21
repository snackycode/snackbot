package com.stattracker.domain.model;

import java.time.Instant;
import java.time.LocalTime;

/**
 * Subscription configuration for a Discord user.
 * Controls premium features and automated daily stat posts.
 */
public class Subscription {

    private String id;
    private String discordUserId;
    private String discordChannelId;
    private SubscriptionTier tier;
    private boolean dailyPostEnabled;
    private LocalTime dailyPostTime;
    private Game dailyPostGame;
    private Instant createdAt;
    private Instant expiresAt;

    public enum SubscriptionTier {
        FREE,
        PREMIUM,
        PREMIUM_PLUS
    }

    public Subscription() {
    }

    public Subscription(String discordUserId, SubscriptionTier tier) {
        this.discordUserId = discordUserId;
        this.tier = tier;
        this.dailyPostEnabled = false;
        this.dailyPostTime = LocalTime.of(9, 0); // default 09:00
        this.createdAt = Instant.now();
    }

    // ── Getters ──────────────────────────────────────────

    public String getId() {
        return id;
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    public SubscriptionTier getTier() {
        return tier;
    }

    public boolean isDailyPostEnabled() {
        return dailyPostEnabled;
    }

    public LocalTime getDailyPostTime() {
        return dailyPostTime;
    }

    public Game getDailyPostGame() {
        return dailyPostGame;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    // ── Setters ──────────────────────────────────────────

    public void setId(String id) {
        this.id = id;
    }

    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public void setDiscordChannelId(String discordChannelId) {
        this.discordChannelId = discordChannelId;
    }

    public void setTier(SubscriptionTier tier) {
        this.tier = tier;
    }

    public void setDailyPostEnabled(boolean dailyPostEnabled) {
        this.dailyPostEnabled = dailyPostEnabled;
    }

    public void setDailyPostTime(LocalTime dailyPostTime) {
        this.dailyPostTime = dailyPostTime;
    }

    public void setDailyPostGame(Game dailyPostGame) {
        this.dailyPostGame = dailyPostGame;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    // ── Domain logic ────────────────────────────────────

    public boolean isPremium() {
        return tier == SubscriptionTier.PREMIUM || tier == SubscriptionTier.PREMIUM_PLUS;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public void enableDailyPost(String channelId, Game game, LocalTime time) {
        this.dailyPostEnabled = true;
        this.discordChannelId = channelId;
        this.dailyPostGame = game;
        this.dailyPostTime = time;
    }

    public void disableDailyPost() {
        this.dailyPostEnabled = false;
    }
}
