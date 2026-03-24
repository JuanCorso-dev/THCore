package com.thteam.thcore.item;

import com.thteam.thcore.message.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for ItemStack.
 *
 * Usage:
 *   ItemStack item = new ItemBuilder(Material.DIAMOND)
 *       .name("<aqua>VIP Diamond")
 *       .lore("<gray>Click to buy", "<yellow>Cost: $500")
 *       .amount(3)
 *       .enchant(Enchantment.SHARPNESS, 1)
 *       .glow(true)
 *       .model(1234)
 *       .unbreakable(true)
 *       .build();
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    // ---------------------------------------------------------------- Constructors

    /**
     * Creates a new ItemBuilder with the given material (amount = 1).
     */
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    /**
     * Creates an ItemBuilder from a copy of an existing ItemStack.
     * Modifying the builder will not affect the original.
     */
    public ItemBuilder(ItemStack base) {
        this.item = base.clone();
        this.meta = this.item.getItemMeta();
    }

    // ---------------------------------------------------------------- Builder methods

    /**
     * Sets the display name. Supports MiniMessage tags and legacy &-codes.
     */
    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.displayName(MessageUtil.colorize(name).decoration(TextDecoration.ITALIC, false));
        }
        return this;
    }

    /**
     * Sets the lore lines. Each line supports MiniMessage tags and legacy &-codes.
     */
    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    /**
     * Sets the lore lines from a list. Each line supports MiniMessage tags and legacy &-codes.
     */
    public ItemBuilder lore(List<String> lines) {
        if (meta != null) {
            List<Component> components = new ArrayList<>();
            for (String line : lines) {
                components.add(MessageUtil.colorize(line).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(components);
        }
        return this;
    }

    /**
     * Sets the item amount (clamped to 1–64).
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    /**
     * Adds an enchantment (unsafe — allows any level and combination).
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    /**
     * Adds one or more ItemFlags.
     */
    public ItemBuilder flag(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    /**
     * Adds a visual enchantment glow effect without showing any enchantment tooltip.
     * Uses LUCK level 1 + HIDE_ENCHANTS flag.
     */
    public ItemBuilder glow(boolean glow) {
        if (meta != null) {
            if (glow) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.removeEnchant(Enchantment.LUCK_OF_THE_SEA);
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }
        return this;
    }

    /**
     * Sets the CustomModelData value (used by resource packs to select custom models).
     */
    public ItemBuilder model(int modelData) {
        if (meta != null) {
            meta.setCustomModelData(modelData);
        }
        return this;
    }

    /**
     * Sets whether the item is unbreakable.
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    // ---------------------------------------------------------------- Build

    /**
     * Builds and returns the final ItemStack.
     */
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
