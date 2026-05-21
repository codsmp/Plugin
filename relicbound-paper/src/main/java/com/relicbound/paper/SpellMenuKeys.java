package com.relicbound.paper;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpellMenuKeys {
    private SpellMenuKeys() {
    }

    public static NamespacedKey spellIdKey(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "spell_id");
    }
}
