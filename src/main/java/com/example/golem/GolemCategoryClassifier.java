package com.example.golem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class GolemCategoryClassifier {

    private static final Set<String> WOOD_ITEMS = Set.of(
            "oak_log", "spruce_log", "birch_log", "jungle_log", "acacia_log", "dark_oak_log", "mangrove_log", "cherry_log", "pale_oak_log",
            "oak_wood", "spruce_wood", "birch_wood", "jungle_wood", "acacia_wood", "dark_oak_wood", "mangrove_wood", "cherry_wood", "pale_oak_wood",
            "stripped_oak_log", "stripped_spruce_log", "stripped_birch_log", "stripped_jungle_log", "stripped_acacia_log", "stripped_dark_oak_log", "stripped_mangrove_log", "stripped_cherry_log", "stripped_pale_oak_log",
            "oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks", "mangrove_planks", "cherry_planks", "pale_oak_planks",
            "oak_stairs", "spruce_stairs", "birch_stairs", "jungle_stairs", "acacia_stairs", "dark_oak_stairs", "mangrove_stairs", "cherry_stairs", "pale_oak_stairs",
            "oak_slab", "spruce_slab", "birch_slab", "jungle_slab", "acacia_slab", "dark_oak_slab", "mangrove_slab", "cherry_slab", "pale_oak_slab",
            "oak_fence", "spruce_fence", "birch_fence", "jungle_fence", "acacia_fence", "dark_oak_fence", "mangrove_fence", "cherry_fence", "pale_oak_fence",
            "oak_fence_gate", "spruce_fence_gate", "birch_fence_gate", "jungle_fence_gate", "acacia_fence_gate", "dark_oak_fence_gate", "mangrove_fence_gate", "cherry_fence_gate", "pale_oak_fence_gate",
            "oak_door", "spruce_door", "birch_door", "jungle_door", "acacia_door", "dark_oak_door", "mangrove_door", "cherry_door", "pale_oak_door",
            "oak_trapdoor", "spruce_trapdoor", "birch_trapdoor", "jungle_trapdoor", "acacia_trapdoor", "dark_oak_trapdoor", "mangrove_trapdoor", "cherry_trapdoor", "pale_oak_trapdoor",
            "oak_button", "spruce_button", "birch_button", "jungle_button", "acacia_button", "dark_oak_button", "mangrove_button", "cherry_button", "pale_oak_button",
            "oak_pressure_plate", "spruce_pressure_plate", "birch_pressure_plate", "jungle_pressure_plate", "acacia_pressure_plate", "dark_oak_pressure_plate", "mangrove_pressure_plate", "cherry_pressure_plate", "pale_oak_pressure_plate",
            "oak_sign", "spruce_sign", "birch_sign", "jungle_sign", "acacia_sign", "dark_oak_sign", "mangrove_sign", "cherry_sign", "pale_oak_sign",
            "oak_hanging_sign", "spruce_hanging_sign", "birch_hanging_sign", "jungle_hanging_sign", "acacia_hanging_sign", "dark_oak_hanging_sign", "mangrove_hanging_sign", "cherry_hanging_sign", "pale_oak_hanging_sign",
            "oak_boat", "spruce_boat", "birch_boat", "jungle_boat", "acacia_boat", "dark_oak_boat", "mangrove_boat", "cherry_boat", "pale_oak_boat",
            "oak_chest_boat", "spruce_chest_boat", "birch_chest_boat", "jungle_chest_boat", "acacia_chest_boat", "dark_oak_chest_boat", "mangrove_chest_boat", "cherry_chest_boat", "pale_oak_chest_boat",
            "stick", "bowl", "chest", "trapped_chest", "barrel", "crafting_table", "bookshelf", "chiseled_bookshelf", "lectern", "composter", "note_block", "jukebox", "cartography_table", "fletching_table", "smithing_table", "loom",
            "beehive", "bee_nest", "campfire", "soul_campfire", "item_frame", "painting", "armor_stand",  "charcoal"
    );

    private static final Set<String> FOOD_ITEMS = Set.of(
            "apple", "golden_apple", "enchanted_golden_apple",
            "melon_slice", "sweet_berries", "glow_berries",
            "carrot", "golden_carrot", "potato", "baked_potato", "poisonous_potato",
            "beetroot", "beetroot_soup",
            "bread", "cookie", "cake", "pumpkin_pie",
            "mushroom_stew", "rabbit_stew", "suspicious_stew",
            "cooked_beef", "beef",
            "cooked_porkchop", "porkchop",
            "cooked_chicken", "chicken",
            "cooked_mutton", "mutton",
            "cooked_rabbit", "rabbit",
            "cooked_cod", "cod",
            "cooked_salmon", "salmon",
            "tropical_fish", "pufferfish",
            "rotten_flesh",
            "raw_cod", "raw_salmon",
            "egg",
            "milk_bucket",
            "honey_bottle",
            "dried_kelp", "wheat", "wheat_seeds", "sugar", "sugar_cane",
            "cocoa_beans", "pumpkin", "melon",
            "brown_mushroom", "red_mushroom",
            "beetroot_seeds",
            "kelp"
    );

    private static final Set<String> ORE_ITEMS = Set.of(
            "coal_ore", "deepslate_coal_ore",
            "iron_ore", "deepslate_iron_ore",
            "copper_ore", "deepslate_copper_ore",
            "gold_ore", "deepslate_gold_ore",
            "redstone_ore", "deepslate_redstone_ore",
            "lapis_ore", "deepslate_lapis_ore",
            "diamond_ore", "deepslate_diamond_ore",
            "emerald_ore", "deepslate_emerald_ore",
            "nether_gold_ore",
            "nether_quartz_ore",
            "ancient_debris",
            "coal", "charcoal",
            "raw_iron", "raw_copper", "raw_gold",
            "iron_ingot", "copper_ingot", "gold_ingot",
            "diamond", "emerald", "lapis_lazuli",
            "redstone", "quartz", "netherite_ingot",
            "netherite_scrap",
            "amethyst_shard",
            "coal_block", "iron_block", "copper_block",
            "gold_block", "redstone_block", "lapis_block",
            "diamond_block", "emerald_block",
            "raw_iron_block", "raw_copper_block",
            "raw_gold_block", "netherite_block",
            "amethyst_block",
            "budding_amethyst",
            "amethyst_cluster",
            "small_amethyst_bud",
            "medium_amethyst_bud",
            "large_amethyst_bud",
            "tuff",
            "calcite",
            "dripstone_block"
    );

    private static final Set<String> BUILDING_ITEMS = Set.of(
            "stone", "cobblestone", "mossy_cobblestone",
            "stone_bricks", "mossy_stone_bricks",
            "cracked_stone_bricks", "chiseled_stone_bricks",
            "smooth_stone", "stone_slab", "stone_stairs",
            "cobblestone_slab", "cobblestone_stairs",
            "mossy_cobblestone_slab", "mossy_cobblestone_stairs",
            "andesite", "polished_andesite", "andesite_slab", "andesite_stairs",
            "diorite", "polished_diorite", "diorite_slab", "diorite_stairs",
            "granite", "polished_granite", "granite_slab", "granite_stairs",
            "polished_tuff", "tuff_bricks",
            "dripstone_block",
            "sand", "red_sand", "sandstone", "red_sandstone",
            "smooth_sandstone", "smooth_red_sandstone",
            "cut_sandstone", "cut_red_sandstone",
            "chiseled_sandstone", "chiseled_red_sandstone",
            "sandstone_bricks", "red_sandstone_bricks",
            "gravel", "dirt", "coarse_dirt", "rooted_dirt",
            "grass_block", "podzol", "mycelium",
            "mud", "packed_mud", "mud_bricks",
            "clay", "clay_ball", "terracotta",
            "white_terracotta", "orange_terracotta", "magenta_terracotta",
            "light_blue_terracotta", "yellow_terracotta", "lime_terracotta",
            "pink_terracotta", "gray_terracotta", "light_gray_terracotta",
            "cyan_terracotta", "purple_terracotta", "blue_terracotta",
            "brown_terracotta", "green_terracotta", "red_terracotta",
            "black_terracotta",
            "white_concrete", "orange_concrete", "magenta_concrete",
            "light_blue_concrete", "yellow_concrete", "lime_concrete",
            "pink_concrete", "gray_concrete", "light_gray_concrete",
            "cyan_concrete", "purple_concrete", "blue_concrete",
            "brown_concrete", "green_concrete", "red_concrete",
            "black_concrete",
            "white_concrete_powder", "orange_concrete_powder",
            "magenta_concrete_powder", "light_blue_concrete_powder",
            "yellow_concrete_powder", "lime_concrete_powder",
            "pink_concrete_powder", "gray_concrete_powder",
            "light_gray_concrete_powder", "cyan_concrete_powder",
            "purple_concrete_powder", "blue_concrete_powder",
            "brown_concrete_powder", "green_concrete_powder",
            "red_concrete_powder", "black_concrete_powder",
            "bricks", "brick_slab", "brick_stairs", "brick_wall",
            "stone_brick_wall", "cobblestone_wall",
            "mossy_cobblestone_wall", "mossy_stone_brick_wall",
            "granite_wall", "diorite_wall", "andesite_wall",
            "prismarine", "prismarine_bricks",
            "dark_prismarine", "prismarine_slab",
            "prismarine_brick_slab", "dark_prismarine_slab",
            "prismarine_stairs", "prismarine_brick_stairs",
            "dark_prismarine_stairs",
            "sea_lantern", "glowstone",
            "glass", "glass_pane",
            "white_stained_glass", "orange_stained_glass",
            "magenta_stained_glass", "light_blue_stained_glass",
            "yellow_stained_glass", "lime_stained_glass",
            "pink_stained_glass", "gray_stained_glass",
            "light_gray_stained_glass", "cyan_stained_glass",
            "purple_stained_glass", "blue_stained_glass",
            "brown_stained_glass", "green_stained_glass",
            "red_stained_glass", "black_stained_glass",
            "torch", "soul_torch", "lantern", "soul_lantern",
            "end_rod", "shroomlight", "moss_block", "moss_carpet",
            "snow_block", "ice", "packed_ice", "blue_ice",
            "hay_block", "honeycomb_block", "honey_block",
            "slime_block", "sponge", "wet_sponge",
            "bone_block", "dried_kelp_block",
            "target", "scaffolding",
            "iron_bars", "chain", "ladder",
            "white_carpet", "orange_carpet", "magenta_carpet",
            "light_blue_carpet", "yellow_carpet", "lime_carpet",
            "pink_carpet", "gray_carpet", "light_gray_carpet",
            "cyan_carpet", "purple_carpet", "blue_carpet",
            "brown_carpet", "green_carpet", "red_carpet",
            "black_carpet"

    );

    private static final Set<String> NETHER_WORLD_ITEMS = Set.of(
            "netherrack", "nether_bricks", "red_nether_bricks",
            "cracked_nether_bricks", "chiseled_nether_bricks",
            "nether_brick_slab", "nether_brick_stairs",
            "nether_brick_wall", "red_nether_brick_slab",
            "red_nether_brick_stairs", "red_nether_brick_wall",
            "basalt", "polished_basalt", "smooth_basalt",
            "blackstone", "polished_blackstone",
            "polished_blackstone_bricks",
            "cracked_polished_blackstone_bricks",
            "chiseled_polished_blackstone",
            "gilded_blackstone",
            "end_stone", "end_stone_bricks",
            "purpur_block", "purpur_pillar",
            "purpur_slab", "purpur_stairs"
    );

    private static final Set<String> ARMOR_ITEMS = Set.of(
            "leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots",
            "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots",
            "iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots",
            "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots",
            "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots",
            "chainmail_helmet", "chainmail_chestplate", "chainmail_leggings", "chainmail_boots",
            "turtle_helmet",
            "leather_horse_armor", "iron_horse_armor", "golden_horse_armor", "diamond_horse_armor",
            "wolf_armor",
            "netherite_upgrade_smithing_template",
            "coast_armor_trim_smithing_template",
            "dune_armor_trim_smithing_template",
            "eye_armor_trim_smithing_template",
            "host_armor_trim_smithing_template",
            "raiser_armor_trim_smithing_template",
            "rib_armor_trim_smithing_template",
            "sentry_armor_trim_smithing_template",
            "shaper_armor_trim_smithing_template",
            "silence_armor_trim_smithing_template",
            "snout_armor_trim_smithing_template",
            "spire_armor_trim_smithing_template",
            "tide_armor_trim_smithing_template",
            "vex_armor_trim_smithing_template",
            "ward_armor_trim_smithing_template",
            "wayfinder_armor_trim_smithing_template",
            "wild_armor_trim_smithing_template",
            "bolt_armor_trim_smithing_template",
            "flow_armor_trim_smithing_template",
            "shield", "elytra", "carved_pumpkin",
            "copper_helmet",
            "copper_chestplate",
            "copper_leggings",
            "copper_boots", "copper_horse_armor", "copper_nautilus_armor"
    );

    private static final Set<String> WEAPON_ITEMS = Set.of(
            "wooden_sword", "stone_sword", "iron_sword", "golden_sword", "diamond_sword", "netherite_sword",
            "bow", "crossbow", "trident", "mace",
            "arrow", "spectral_arrow", "tipped_arrow",
            "magicwand", "sniper", "sniper_rifle",
            "fire_charge", "wind_charge",
            "tnt",
            "end_crystal", "void_core"
    );



    private static final Set<String> TOOLS_ITEMS = Set.of(
            "wooden_pickaxe", "wooden_axe", "wooden_shovel", "wooden_hoe",
            "stone_pickaxe", "stone_axe", "stone_shovel", "stone_hoe",
            "iron_pickaxe", "iron_axe", "iron_shovel", "iron_hoe",
            "golden_pickaxe", "golden_axe", "golden_shovel", "golden_hoe",
            "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe",
            "netherite_pickaxe", "netherite_axe", "netherite_shovel", "netherite_hoe",
            "fishing_rod", "flint_and_steel",
            "shears", "brush", "compass", "recovery_compass", "clock",
            "spyglass", "carrot_on_a_stick", "warped_fungus_on_a_stick",
            "lead", "name_tag", "firework_rocket"
    );

    private static final Set<String> POTIONS_ITEMS = Set.of(
            "potion", "splash_potion", "lingering_potion", "tipped_arrow",
            "water", "mundane", "thick", "awkward",
            "night_vision", "long_night_vision",
            "invisibility", "long_invisibility",
            "leaping", "long_leaping", "strong_leaping",
            "fire_resistance", "long_fire_resistance",
            "swiftness", "long_swiftness", "strong_swiftness",
            "slowness", "long_slowness", "strong_slowness",
            "water_breathing", "long_water_breathing",
            "healing", "strong_healing",
            "harming", "strong_harming",
            "poison", "long_poison", "strong_poison",
            "regeneration", "long_regeneration", "strong_regeneration",
            "strength", "long_strength", "strong_strength",
            "weakness", "long_weakness",
            "turtle_master", "long_turtle_master", "strong_turtle_master",
            "slow_falling", "long_slow_falling",
            "luck",
            "infested", "oozing", "weaving", "wind_charged",
            "glass_bottle", "nether_wart", "blaze_powder",
            "glistering_melon_slice", "magma_cream", "ghast_tear",
            "rabbit_foot", "fermented_spider_eye",
            "spider_eye",
            "phantom_membrane", "turtle_scute",
            "breeze_rod", "cobweb",
            "glowstone_dust", "gunpowder",
            "dragon_breath"
    );

    public static GolemCategory getCategory(ItemStack stack) {

        if (stack.isEmpty()) {
            return GolemCategory.MISC;
        }

        String id = BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .getPath();


        if (containsAny(id, WOOD_ITEMS)) {
            return GolemCategory.WOOD;
        }

        if (containsAny(id, FOOD_ITEMS)) {
            return GolemCategory.FOOD;
        }

        if (containsAny(id, ORE_ITEMS)) {
            return GolemCategory.ORES;
        }

        if (containsAny(id, BUILDING_ITEMS)) {
            return GolemCategory.BUILDING;
        }

        if (containsAny(id, NETHER_WORLD_ITEMS)) {
            return GolemCategory.NETHER_WORLD;
        }

        if (containsAny(id, ARMOR_ITEMS)) {
            return GolemCategory.ARMOR;
        }

        if (containsAny(id, WEAPON_ITEMS)) {
            return GolemCategory.WEAPON;
        }

        if (containsAny(id, POTIONS_ITEMS)) {
            return GolemCategory.POTIONS;
        }

        if (containsAny(id, TOOLS_ITEMS)) {
            return GolemCategory.TOOLS;
        }

        return GolemCategory.MISC;
    }

    private static boolean containsAny(String id, Set<String> keywords) {
        return keywords.contains(id);
    }
}