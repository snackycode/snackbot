package com.stattracker.image.template;

import com.stattracker.domain.model.Game;
import java.awt.Color;

/**
 * Themes mapping specific color schemes and styling parameters to each game
 * for rendering visual stats cards.
 */
public enum CardTheme {
    VALORANT_THEME(
            Game.VALORANT,
            new Color(0xFF4654),  // Main Accent
            new Color(0x1F2326),  // Background dark
            new Color(0x16191B),  // Background panel
            new Color(0xFFFFFF),  // Text primary
            new Color(0x7E8287)   // Text secondary
    ),
    LEAGUE_THEME(
            Game.LEAGUE,
            new Color(0x0BC6E3),
            new Color(0x0A0E13),
            new Color(0x121A23),
            new Color(0xF0E6D2),
            new Color(0xA09B8C)
    ),
    APEX_THEME(
            Game.APEX,
            new Color(0xDA292A),
            new Color(0x151515),
            new Color(0x222222),
            new Color(0xFFFFFF),
            new Color(0xAEAEAE)
    ),
    FORTNITE_THEME(
            Game.FORTNITE,
            new Color(0x9D4DBB),
            new Color(0x110E2E),
            new Color(0x1E1B4B),
            new Color(0xFDE047),
            new Color(0x93C5FD)
    );

    private final Game game;
    private final Color primaryColor;
    private final Color backgroundColor;
    private final Color panelColor;
    private final Color textColor;
    private final Color textSecondaryColor;

    CardTheme(Game game, Color primaryColor, Color backgroundColor, Color panelColor, Color textColor, Color textSecondaryColor) {
        this.game = game;
        this.primaryColor = primaryColor;
        this.backgroundColor = backgroundColor;
        this.panelColor = panelColor;
        this.textColor = textColor;
        this.textSecondaryColor = textSecondaryColor;
    }

    public Game getGame() { return game; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getPanelColor() { return panelColor; }
    public Color getTextColor() { return textColor; }
    public Color getTextSecondaryColor() { return textSecondaryColor; }

    public static CardTheme getThemeFor(Game game) {
        for (CardTheme theme : values()) {
            if (theme.game == game) {
                return theme;
            }
        }
        return VALORANT_THEME;
    }
}
