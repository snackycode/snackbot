package com.stattracker.discord.config;
import com.stattracker.application.config.ExternalSecrets;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds and exposes the {@link JDA} singleton bean.
 * The Discord bot token is read from {@code stattracker.discord.token}
 * (typically set via environment variable or application.yml).
 */
@Configuration
public class JdaConfig {

    private static final Logger log = LoggerFactory.getLogger(JdaConfig.class);

    private String botToken;

    public JdaConfig(ExternalSecrets secrets) {
        this.botToken = secrets.getDiscordToken();   
    }

    @Bean
    public JDA jda() throws InterruptedException {
        log.info("Initializing JDA with gateway intents…");
        JDA jda = JDABuilder.createDefault(botToken)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                .setChunkingFilter(ChunkingFilter.NONE)
                .build()
                .awaitReady();

        log.info("JDA ready — connected as {}", jda.getSelfUser().getAsTag());
        return jda;
    }
}
