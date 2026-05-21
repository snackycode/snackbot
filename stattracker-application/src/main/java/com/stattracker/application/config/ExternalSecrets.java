package com.stattracker.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Central place for all third-party credentials.
 * Values are injected from {@code application.yml} or from environment variables.
 */
@Component
@ConfigurationProperties(prefix = "external")
public class ExternalSecrets {

    /** Discord bot token (used by JdaConfig) */
    private String discordToken;

    /** Riot Games API key (used by RiotApiClient) */
    private String riotApiKey;

    /** Apex Tracker.gg API key */
    private String apexApiKey;

    /** Fortnite API key */
    private String fortniteApiKey;

    // getters & setters

    public String getDiscordToken() { return discordToken; }
    public void setDiscordToken(String discordToken) { this.discordToken = discordToken; }

    public String getRiotApiKey() { return riotApiKey; }
    public void setRiotApiKey(String riotApiKey) { this.riotApiKey = riotApiKey; }

    public String getApexApiKey() { return apexApiKey; }
    public void setApexApiKey(String apexApiKey) { this.apexApiKey = apexApiKey; }

    public String getFortniteApiKey() { return fortniteApiKey; }
    public void setFortniteApiKey(String fortniteApiKey) { this.fortniteApiKey = fortniteApiKey; }
}
