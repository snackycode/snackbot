package com.stattracker.domain.model;

import java.time.Instant;

/**
 * Links a Discord user to their in-game account for a specific game.
 * One Discord user may have multiple linked accounts across different games.
 */
public class LinkedAccount {

    private String id;
    private String discordUserId;
    private Game game;
    private String gameUsername;
    private String region;
    private Instant linkedAt;
    private boolean verified;

    public LinkedAccount() {
    }

    public LinkedAccount(String discordUserId, Game game, String gameUsername, String region) {
        this.discordUserId = discordUserId;
        this.game = game;
        this.gameUsername = gameUsername;
        this.region = region;
        this.linkedAt = Instant.now();
        this.verified = false;
    }

    // ── Getters ──────────────────────────────────────────

    public String getId() {
        return id;
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public Game getGame() {
        return game;
    }

    public String getGameUsername() {
        return gameUsername;
    }

    public String getRegion() {
        return region;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public boolean isVerified() {
        return verified;
    }

    // ── Setters ──────────────────────────────────────────

    public void setId(String id) {
        this.id = id;
    }

    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setGameUsername(String gameUsername) {
        this.gameUsername = gameUsername;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Mark this account as verified (e.g. after an in-game verification step).
     */
    public void verify() {
        this.verified = true;
    }

    @Override
    public String toString() {
        return "LinkedAccount{" +
                "discordUserId='" + discordUserId + '\'' +
                ", game=" + game +
                ", gameUsername='" + gameUsername + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
