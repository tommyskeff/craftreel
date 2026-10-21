package dev.tommyjs.craftreel.replay.base.handler.navigation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.tommyjs.craftreel.replay.reference.ReplayPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

public final class ReplayNavigation {

    public static final int COMPASS_SLOT = 0;
    public static final String COMPASS_NAME = "Spectate";

    private ReplayNavigation() {
    }

    public static boolean isCompass(@Nullable ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName()
            && COMPASS_NAME.equals(ChatColor.stripColor(meta.getDisplayName()));
    }

    public static ItemStack compass() {
        return named(new ItemStack(Material.COMPASS), ChatColor.AQUA + COMPASS_NAME,
            ChatColor.GRAY + "Left-click to teleport to the next player",
            ChatColor.GRAY + "Right-click to pick a player",
            ChatColor.GRAY + "Right-click a player to see their view");
    }

    public static ItemStack head(ReplayPlayer player, boolean following) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (player.skinValue() == null) {
            meta.setOwner(player.name());
        } else {
            GameProfile profile = new GameProfile(UUID.randomUUID(), player.name());
            profile.getProperties().put("textures",
                new Property("textures", player.skinValue(), player.skinSignature()));
            applyProfile(meta, profile);
        }

        item.setItemMeta(meta);
        return named(item, (following ? ChatColor.GREEN : ChatColor.AQUA) + player.name(),
            following
                ? ChatColor.GRAY + "Currently seeing their view"
                : ChatColor.GRAY + "Click to teleport to them");
    }

    public static ItemStack freeCamera(boolean active) {
        return named(new ItemStack(Material.BARRIER),
            (active ? ChatColor.GREEN : ChatColor.RED) + "Free camera",
            active
                ? ChatColor.GRAY + "Currently free"
                : ChatColor.GRAY + "Click to stop following");
    }

    private static void applyProfile(SkullMeta meta, GameProfile profile) {
        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to apply skull texture", e);
        }
    }

    private static ItemStack named(ItemStack item, String name, String... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

}
