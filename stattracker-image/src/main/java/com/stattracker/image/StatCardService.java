package com.stattracker.image;

import com.stattracker.domain.model.PlayerStats;
import org.springframework.stereotype.Service;

@Service
public class StatCardService {

    private final StatCardRenderer renderer;

    public StatCardService(StatCardRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Accepts normalized PlayerStats and returns a rendered PNG byte array.
     */
    public byte[] generateCard(PlayerStats stats) {
        if (stats == null) {
            throw new IllegalArgumentException("PlayerStats cannot be null for card generation");
        }
        return renderer.render(stats);
    }
}
