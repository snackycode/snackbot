package com.stattracker.discord.embed;

import com.stattracker.domain.model.Game;
import com.stattracker.domain.model.MatchSummary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds rich embeds for the {@code /history} command.
 */
@Component
public class HistoryEmbedBuilder {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("MMM dd, HH:mm").withZone(ZoneId.of("UTC"));

    /**
     * Build a paginated-style embed showing recent match summaries.
     */
    public MessageEmbed build(String username, Game game, List<MatchSummary> matches) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📜 Match History — " + username)
                .setColor(getGameColor(game))
                .setFooter(game.getDisplayName() + " • Last " + matches.size() + " matches");

        if (matches.isEmpty()) {
            embed.setDescription("No recent matches found for **" + username + "**.");
            return embed.build();
        }

        for (int i = 0; i < matches.size(); i++) {
            MatchSummary m = matches.get(i);
            String outcomeEmoji = switch (m.outcome()) {
                case WIN -> "🟢";
                case LOSS -> "🔴";
                case DRAW -> "🟡";
            };

            String title = String.format("%s %s — %s", outcomeEmoji, m.outcome(), m.mapName());
            String body = String.format("**KDA**: %s  |  **Score**: %d\n**Agent/Legend**: %s  |  **Duration**: %s\n%s",
                    m.kdaString(), m.score(),
                    m.agentOrLegend().isBlank() ? "—" : m.agentOrLegend(),
                    m.durationDisplay(),
                    TIME_FMT.format(m.playedAt()));

            embed.addField(title, body, false);
        }

        return embed.build();
    }

    private Color getGameColor(Game game) {
        return switch (game) {
            case VALORANT -> new Color(0xFF4654);
            case LEAGUE -> new Color(0x0BC6E3);
            case APEX -> new Color(0xDA292A);
            case FORTNITE -> new Color(0x9D4DBB);
        };
    }
}
