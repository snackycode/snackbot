package com.stattracker.image;

import com.stattracker.domain.model.PlayerStats;
import com.stattracker.image.template.CardLayout;
import com.stattracker.image.template.CardTheme;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@Component
public class StatCardRenderer {

    public byte[] render(PlayerStats stats) {
        CardTheme theme = CardTheme.getThemeFor(stats.game());

        // Create canvas
        BufferedImage image = new BufferedImage(
                CardLayout.CARD_WIDTH,
                CardLayout.CARD_HEIGHT,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2 = image.createGraphics();
        
        try {
            // Enable antialiasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw Background Card Panel
            g2.setColor(theme.getBackgroundColor());
            g2.fill(new RoundRectangle2D.Double(0, 0, CardLayout.CARD_WIDTH, CardLayout.CARD_HEIGHT, CardLayout.CORNER_RADIUS, CardLayout.CORNER_RADIUS));

            // Draw Header separator accent strip
            g2.setColor(theme.getPrimaryColor());
            g2.fillRect(0, 0, CardLayout.CARD_WIDTH, 12);

            // 1. Draw Profile Info (Top Area)
            g2.setColor(theme.getTextColor());
            g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.BOLD, CardLayout.FONT_SIZE_TITLE));
            g2.drawString(stats.username(), 40, 70);

            g2.setColor(theme.getTextSecondaryColor());
            g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.PLAIN, CardLayout.FONT_SIZE_SUBTITLE));
            g2.drawString(stats.game().getDisplayName() + " | Level " + stats.level() + " | " + stats.region(), 40, 100);

            // 2. Draw Rank Box (Right Side Panel)
            int rankBoxX = 520;
            int rankBoxY = 50;
            int rankBoxW = 240;
            int rankBoxH = 80;
            g2.setColor(theme.getPanelColor());
            g2.fill(new RoundRectangle2D.Double(rankBoxX, rankBoxY, rankBoxW, rankBoxH, 12, 12));

            g2.setColor(theme.getPrimaryColor());
            g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.BOLD, CardLayout.FONT_SIZE_LABEL));
            g2.drawString("CURRENT COMPETITIVE RANK", rankBoxX + 15, rankBoxY + 25);

            g2.setColor(theme.getTextColor());
            g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.BOLD, CardLayout.FONT_SIZE_RANK));
            String rankStr = stats.rankedInfo() != null ? stats.rankedInfo().displayString() : "UNRANKED";
            g2.drawString(rankStr, rankBoxX + 15, rankBoxY + 55);

            // 3. Draw Grid Stats Panels (Bottom Area)
            // Draw 4 stat boxes (K/D, Win Rate, Total Kills, Total Matches)
            drawStatPanel(g2, theme, "K/D RATIO", String.format("%.2f", stats.kdRatio()), 40, 160, 160, 100);
            drawStatPanel(g2, theme, "WIN RATE", String.format("%.1f%%", stats.winRate() * 100), 220, 160, 160, 100);
            drawStatPanel(g2, theme, "TOTAL KILLS", String.valueOf(stats.totalKills()), 400, 160, 160, 100);
            drawStatPanel(g2, theme, "TOTAL MATCHES", String.valueOf(stats.totalMatches()), 580, 160, 180, 100);

            // 4. Draw Extra Information list
            int extraY = 320;
            g2.setColor(theme.getTextSecondaryColor());
            g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.BOLD, 12));
            g2.drawString("ADDITIONAL METRICS:", 40, extraY);

            g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.PLAIN, 14));
            int col = 0;
            for (var entry : stats.extra().entrySet()) {
                if (col > 3) break; // Limit horizontal columns to fit
                int extraX = 40 + (col * 180);
                g2.setColor(theme.getPrimaryColor());
                g2.drawString("• " + entry.getKey() + ": ", extraX, extraY + 30);
                g2.setColor(theme.getTextColor());
                g2.drawString(entry.getValue(), extraX + g2.getFontMetrics().stringWidth("• " + entry.getKey() + ": "), extraY + 30);
                col++;
            }

            // Draw generation watermark
            g2.setColor(theme.getTextSecondaryColor());
            g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.ITALIC, 10));
            g2.drawString("StatTracker Bot | Rendered via Graphics2D", CardLayout.CARD_WIDTH - 230, CardLayout.CARD_HEIGHT - 20);

        } finally {
            g2.dispose();
        }

        // Output to byte array
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode rendered stat card image to PNG bytes", e);
        }
    }

    private void drawStatPanel(Graphics2D g2, CardTheme theme, String label, String value, int x, int y, int w, int h) {
        g2.setColor(theme.getPanelColor());
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));

        g2.setColor(theme.getTextSecondaryColor());
        g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.BOLD, CardLayout.FONT_SIZE_LABEL));
        g2.drawString(label, x + 15, y + 30);

        g2.setColor(theme.getPrimaryColor());
        g2.setFont(new Font(CardLayout.FONT_PRIMARY, Font.BOLD, CardLayout.FONT_SIZE_VALUE));
        g2.drawString(value, x + 15, y + 70);
    }
}
