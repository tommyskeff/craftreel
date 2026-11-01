package dev.tommyjs.craftreel.replay.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum LegacyParticle {

    EXPLOSION_NORMAL(0, "explode", "poof", 0),
    EXPLOSION_LARGE(1, "largeexplode", "explosion", 0),
    EXPLOSION_HUGE(2, "hugeexplosion", "explosion_emitter", 0),
    FIREWORKS_SPARK(3, "fireworksSpark", "firework", 0),
    WATER_BUBBLE(4, "bubble", "bubble", 0),
    WATER_SPLASH(5, "splash", "splash", 0),
    WATER_WAKE(6, "wake", "fishing", 0),
    SUSPENDED(7, "suspended", "underwater", 0),
    SUSPENDED_DEPTH(8, "depthsuspend", "underwater", 0),
    CRIT(9, "crit", "crit", 0),
    CRIT_MAGIC(10, "magicCrit", "enchanted_hit", 0),
    SMOKE_NORMAL(11, "smoke", "smoke", 0),
    SMOKE_LARGE(12, "largesmoke", "large_smoke", 0),
    SPELL(13, "spell", "effect", 0),
    SPELL_INSTANT(14, "instantSpell", "instant_effect", 0),
    SPELL_MOB(15, "mobSpell", "entity_effect", 0),
    SPELL_MOB_AMBIENT(16, "mobSpellAmbient", "ambient_entity_effect", 0),
    SPELL_WITCH(17, "witchMagic", "witch", 0),
    DRIP_WATER(18, "dripWater", "dripping_water", 0),
    DRIP_LAVA(19, "dripLava", "dripping_lava", 0),
    VILLAGER_ANGRY(20, "angryVillager", "angry_villager", 0),
    VILLAGER_HAPPY(21, "happyVillager", "happy_villager", 0),
    TOWN_AURA(22, "townaura", "mycelium", 0),
    NOTE(23, "note", "note", 0),
    PORTAL(24, "portal", "portal", 0),
    ENCHANTMENT_TABLE(25, "enchantmenttable", "enchant", 0),
    FLAME(26, "flame", "flame", 0),
    LAVA(27, "lava", "lava", 0),
    FOOTSTEP(28, "footstep", null, 0),
    CLOUD(29, "cloud", "cloud", 0),
    REDSTONE(30, "reddust", "dust", 0),
    SNOWBALL(31, "snowballpoof", "item_snowball", 0),
    SNOW_SHOVEL(32, "snowshovel", "poof", 0),
    SLIME(33, "slime", "item_slime", 0),
    HEART(34, "heart", "heart", 0),
    BARRIER(35, "barrier", "barrier", 0),
    ITEM_CRACK(36, "iconcrack_", "item", 2),
    BLOCK_CRACK(37, "blockcrack_", "block", 1),
    BLOCK_DUST(38, "blockdust_", "block", 1),
    WATER_DROP(39, "droplet", "rain", 0),
    ITEM_TAKE(40, "take", null, 0),
    MOB_APPEARANCE(41, "mobappearance", "elder_guardian", 0);

    private final int id;
    private final String legacyName;
    private final String modernName;
    private final int argumentCount;

    LegacyParticle(int id, String legacyName, @Nullable String modernName, int argumentCount) {
        this.id = id;
        this.legacyName = legacyName;
        this.modernName = modernName;
        this.argumentCount = argumentCount;
    }

    public static @Nullable LegacyParticle byName(@NotNull String name) {
        for (LegacyParticle particle : values()) {
            if (particle.matches(name)) {
                return particle;
            }
        }
        return null;
    }

    public int id() {
        return id;
    }

    public @Nullable String modernName() {
        return modernName;
    }

    public int @NotNull [] arguments(@NotNull String name) {
        int[] arguments = new int[argumentCount];
        if (argumentCount == 0 || name.length() <= legacyName.length()) {
            return arguments;
        }
        String[] parts = name.substring(legacyName.length()).split("_");
        for (int i = 0; i < Math.min(parts.length, argumentCount); i++) {
            try {
                arguments[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                return new int[argumentCount];
            }
        }
        return arguments;
    }

    private boolean matches(String name) {
        if (legacyName.endsWith("_")) {
            return name.startsWith(legacyName) || name.equals(legacyName.substring(0, legacyName.length() - 1));
        }
        return name.equals(legacyName);
    }

}
