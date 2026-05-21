package com.stattracker.discord.command;

import com.stattracker.discord.autocomplete.GameAutocompleteHandler;
import com.stattracker.discord.embed.ErrorEmbedBuilder;
import com.stattracker.discord.guard.UserRateLimiter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central JDA event listener that routes slash-command events to the correct handler
 * and enforces rate limiting before dispatch.
 */
@Component
public class SlashCommandRegistry extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandRegistry.class);

    private final Map<String, SlashCommandHandler> handlers;
    private final UserRateLimiter rateLimiter;
    private final GameAutocompleteHandler autocompleteHandler;

    public SlashCommandRegistry(List<SlashCommandHandler> handlerList,
                                 UserRateLimiter rateLimiter,
                                 GameAutocompleteHandler autocompleteHandler) {
        this.handlers = new HashMap<>();
        for (SlashCommandHandler handler : handlerList) {
            handlers.put(handler.getCommandName(), handler);
        }
        this.rateLimiter = rateLimiter;
        this.autocompleteHandler = autocompleteHandler;
        log.info("Registered {} slash command handlers: {}", handlers.size(), handlers.keySet());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String userId = event.getUser().getId();

        // Rate limit check
        if (!rateLimiter.tryAcquire(userId)) {
            event.replyEmbeds(ErrorEmbedBuilder.rateLimited()).setEphemeral(true).queue();
            log.warn("Rate limited user {} on /{}", userId, commandName);
            return;
        }

        SlashCommandHandler handler = handlers.get(commandName);
        if (handler == null) {
            event.replyEmbeds(ErrorEmbedBuilder.unknownCommand(commandName)).setEphemeral(true).queue();
            log.warn("No handler for /{}", commandName);
            return;
        }

        try {
            log.debug("Dispatching /{} from user {}", commandName, userId);
            handler.handle(event);
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(ErrorEmbedBuilder.badRequest(e.getMessage())).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Error handling /{}: {}", commandName, e.getMessage(), e);
            event.replyEmbeds(ErrorEmbedBuilder.internalError()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        autocompleteHandler.handle(event);
    }
}
