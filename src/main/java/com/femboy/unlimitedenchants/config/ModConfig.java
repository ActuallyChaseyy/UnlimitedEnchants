package com.femboy.unlimitedenchants.config;

import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.femboy.unlimitedenchants.UnlimitedEnchants.LOGGER;
import static com.femboy.unlimitedenchants.UnlimitedEnchants.MOD_ID;

public final class ModConfig {
	private static final int DEFAULT_MAX_LEVEL = 20;
	// Matches Enchantment.MAX_LEVEL, the absolute ceiling the game's data format allows.
	private static final int MAX_LEVEL_CEILING = 255;

	private static final int DEFAULT_MAX_BOOKSHELVES = 25;
	// Vanilla's own floor for bookshelf power; going lower would only weaken the table.
	private static final int MIN_BOOKSHELVES = 15;
	// The enchanting table only scans a fixed ring of positions (two layers around
	// the table), so bookshelves beyond this count are physically undetectable.
	private static final int MAX_BOOKSHELVES_CEILING = 32;

	private static final boolean DEFAULT_NO_ANVIL_LIMIT = false;

	private static final int DEFAULT_MAX_ANVIL_COST = 39;
	// Kept well clear of Integer.MAX_VALUE, which the anvil mixin uses internally
	// to mean "unreachable" when noAnvilLimit is on.
	private static final int MAX_ANVIL_COST_CEILING = Integer.MAX_VALUE - 1;

	private static final boolean DEFAULT_EXCLUDE_LOOT_FROM_BOOST = true;

	private static final int DEFAULT_MAX_UNLIMITED_ANVIL_COST = 100;
	// Same reasoning as MAX_ANVIL_COST_CEILING.
	private static final int MAX_UNLIMITED_ANVIL_COST_CEILING = Integer.MAX_VALUE - 1;

	// Defaults to the same ceiling as maxLevel itself, so leaving this unset never
	// introduces an extra restriction beyond whatever maxLevel already allows.
	private static final int DEFAULT_MAX_TABLE_ENCHANT = MAX_LEVEL_CEILING;

	private static final String DEFAULT_CONTENT = """
			# Unlimited Enchants configuration
			#
			# maxLevel controls the highest level any enchantment can reach, via the
			# enchanting table, anvil combining, commands (/enchant), and loot.
			# Vanilla enchantments normally cap out around 4-5. Must be between 1 and 255.
			maxLevel: 20

			# maxBookshelves controls how many bookshelves around the enchanting table
			# contribute power. Vanilla caps this at 15, which also caps the highest
			# possible enchanting cost at 30 levels. Raising it raises that cost ceiling
			# too (roughly maxBookshelves x2 levels), so reaching the very top of
			# maxLevel through the table takes more bookshelves than vanilla's 15. The
			# table can only physically detect up to 32 bookshelves (two full rings
			# around it), so values above that have no further effect. Must be between
			# 15 and 32.
			maxBookshelves: 25

			# noAnvilLimit disables vanilla's "Too Expensive!" anvil limit entirely when
			# true, letting an anvil operation cost any number of levels as long as the
			# player can pay it. When false (default), the limit is enforced using
			# maxAnvilCost below instead of vanilla's fixed value.
			noAnvilLimit: false

			# maxAnvilCost is the highest level cost an anvil operation can reach before
			# it's blocked as "Too Expensive!". Only used while noAnvilLimit is false.
			# Vanilla's own effective limit is 39.
			maxAnvilCost: 39

			# maxUnlimitedAnvilCost is a hard cap on the level cost an anvil operation can
			# actually charge while noAnvilLimit is true. Without this, repeatedly
			# repairing/combining the same item in an anvil compounds vanilla's own
			# "prior work penalty" (it roughly doubles each time), so cost would otherwise
			# keep climbing without bound. Only used while noAnvilLimit is true.
			maxUnlimitedAnvilCost: 100

			# maxTableEnchant is the highest level the enchanting table can offer,
			# regardless of maxLevel or bookshelf count. For example, setting this to 10
			# means the table can never offer higher than Sharpness X, even if maxLevel
			# and bookshelves would otherwise allow more. Anvil combining, /enchant, and
			# loot are unaffected. Must be between 1 and 255.
			maxTableEnchant: 255

			# excludeLootFromBoost keeps loot-generated enchantments (chest loot, mob
			# equipment, fishing, etc.) at their normal vanilla levels when true, even
			# though the enchanting table, anvil, and /enchant can reach maxLevel.
			# Villager trades are never affected by this either way - they always use
			# the boosted range.
			excludeLootFromBoost: true
			""";

	private static volatile int maxLevel = DEFAULT_MAX_LEVEL;
	private static volatile int maxBookshelves = DEFAULT_MAX_BOOKSHELVES;
	private static volatile boolean noAnvilLimit = DEFAULT_NO_ANVIL_LIMIT;
	private static volatile int maxAnvilCost = DEFAULT_MAX_ANVIL_COST;
	private static volatile int maxUnlimitedAnvilCost = DEFAULT_MAX_UNLIMITED_ANVIL_COST;
	private static volatile int maxTableEnchant = DEFAULT_MAX_TABLE_ENCHANT;
	private static volatile boolean excludeLootFromBoost = DEFAULT_EXCLUDE_LOOT_FROM_BOOST;

	private ModConfig() {
	}

	public static int getMaxLevel() {
		return maxLevel;
	}

	public static int getMaxBookshelves() {
		return maxBookshelves;
	}

	public static boolean isNoAnvilLimit() {
		return noAnvilLimit;
	}

	public static int getMaxAnvilCost() {
		return maxAnvilCost;
	}

	public static int getMaxUnlimitedAnvilCost() {
		return maxUnlimitedAnvilCost;
	}

	public static int getMaxTableEnchant() {
		return maxTableEnchant;
	}

	public static boolean isExcludeLootFromBoost() {
		return excludeLootFromBoost;
	}

	public static void load() {
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve("config.yaml");

		if (!Files.exists(configFile)) {
			writeDefault(configFile);
			maxLevel = DEFAULT_MAX_LEVEL;
			maxBookshelves = DEFAULT_MAX_BOOKSHELVES;
			noAnvilLimit = DEFAULT_NO_ANVIL_LIMIT;
			maxAnvilCost = DEFAULT_MAX_ANVIL_COST;
			maxUnlimitedAnvilCost = DEFAULT_MAX_UNLIMITED_ANVIL_COST;
			maxTableEnchant = DEFAULT_MAX_TABLE_ENCHANT;
			excludeLootFromBoost = DEFAULT_EXCLUDE_LOOT_FROM_BOOST;
		} else {
			Map<?, ?> data = readYaml(configFile);
			maxLevel = readMaxLevel(configFile, data);
			maxBookshelves = readMaxBookshelves(configFile, data);
			noAnvilLimit = readNoAnvilLimit(configFile, data);
			maxAnvilCost = readMaxAnvilCost(configFile, data);
			maxUnlimitedAnvilCost = readMaxUnlimitedAnvilCost(configFile, data);
			maxTableEnchant = readMaxTableEnchant(configFile, data);
			excludeLootFromBoost = readExcludeLootFromBoost(configFile, data);
		}

		LOGGER.info("Unlimited Enchants: maxLevel set to {}, maxBookshelves set to {}", maxLevel, maxBookshelves);
		LOGGER.info("Unlimited Enchants: noAnvilLimit set to {}, maxAnvilCost set to {}", noAnvilLimit, maxAnvilCost);
		LOGGER.info("Unlimited Enchants: maxUnlimitedAnvilCost set to {}", maxUnlimitedAnvilCost);
		LOGGER.info("Unlimited Enchants: maxTableEnchant set to {}", maxTableEnchant);
		LOGGER.info("Unlimited Enchants: excludeLootFromBoost set to {}", excludeLootFromBoost);
	}

	private static void writeDefault(Path configFile) {
		try {
			Files.createDirectories(configFile.getParent());
			Files.writeString(configFile, DEFAULT_CONTENT, StandardCharsets.UTF_8);
			LOGGER.info("Created default config at {}", configFile);
		} catch (IOException e) {
			LOGGER.error("Failed to write default config to {}", configFile, e);
		}
	}

	private static Map<?, ?> readYaml(Path configFile) {
		try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
			return new Yaml().load(reader);
		} catch (IOException e) {
			LOGGER.error("Failed to read config from {}", configFile, e);
		} catch (RuntimeException e) {
			// SnakeYAML throws unchecked exceptions (e.g. YAMLException) on malformed YAML.
			LOGGER.error("Malformed YAML in {}", configFile, e);
		}

		return null;
	}

	private static int readMaxLevel(Path configFile, Map<?, ?> data) {
		Object rawMaxLevel = data == null ? null : data.get("maxLevel");

		if (rawMaxLevel instanceof Number number) {
			return clamp(number.intValue(), 1, MAX_LEVEL_CEILING, "maxLevel");
		}

		LOGGER.warn("Missing or invalid 'maxLevel' in {}, using default of {}", configFile, DEFAULT_MAX_LEVEL);
		return DEFAULT_MAX_LEVEL;
	}

	private static int readMaxBookshelves(Path configFile, Map<?, ?> data) {
		Object rawMaxBookshelves = data == null ? null : data.get("maxBookshelves");

		if (rawMaxBookshelves instanceof Number number) {
			return clamp(number.intValue(), MIN_BOOKSHELVES, MAX_BOOKSHELVES_CEILING, "maxBookshelves");
		}

		LOGGER.warn("Missing or invalid 'maxBookshelves' in {}, using default of {}", configFile, DEFAULT_MAX_BOOKSHELVES);
		return DEFAULT_MAX_BOOKSHELVES;
	}

	private static boolean readNoAnvilLimit(Path configFile, Map<?, ?> data) {
		Object rawNoAnvilLimit = data == null ? null : data.get("noAnvilLimit");

		if (rawNoAnvilLimit instanceof Boolean bool) {
			return bool;
		}

		LOGGER.warn("Missing or invalid 'noAnvilLimit' in {}, using default of {}", configFile, DEFAULT_NO_ANVIL_LIMIT);
		return DEFAULT_NO_ANVIL_LIMIT;
	}

	private static int readMaxAnvilCost(Path configFile, Map<?, ?> data) {
		Object rawMaxAnvilCost = data == null ? null : data.get("maxAnvilCost");

		if (rawMaxAnvilCost instanceof Number number) {
			return clamp(number.intValue(), 1, MAX_ANVIL_COST_CEILING, "maxAnvilCost");
		}

		LOGGER.warn("Missing or invalid 'maxAnvilCost' in {}, using default of {}", configFile, DEFAULT_MAX_ANVIL_COST);
		return DEFAULT_MAX_ANVIL_COST;
	}

	private static int readMaxUnlimitedAnvilCost(Path configFile, Map<?, ?> data) {
		Object rawMaxUnlimitedAnvilCost = data == null ? null : data.get("maxUnlimitedAnvilCost");

		if (rawMaxUnlimitedAnvilCost instanceof Number number) {
			return clamp(number.intValue(), 1, MAX_UNLIMITED_ANVIL_COST_CEILING, "maxUnlimitedAnvilCost");
		}

		LOGGER.warn("Missing or invalid 'maxUnlimitedAnvilCost' in {}, using default of {}",
				configFile, DEFAULT_MAX_UNLIMITED_ANVIL_COST);
		return DEFAULT_MAX_UNLIMITED_ANVIL_COST;
	}

	private static int readMaxTableEnchant(Path configFile, Map<?, ?> data) {
		Object rawMaxTableEnchant = data == null ? null : data.get("maxTableEnchant");

		if (rawMaxTableEnchant instanceof Number number) {
			return clamp(number.intValue(), 1, MAX_LEVEL_CEILING, "maxTableEnchant");
		}

		LOGGER.warn("Missing or invalid 'maxTableEnchant' in {}, using default of {}",
				configFile, DEFAULT_MAX_TABLE_ENCHANT);
		return DEFAULT_MAX_TABLE_ENCHANT;
	}

	private static boolean readExcludeLootFromBoost(Path configFile, Map<?, ?> data) {
		Object rawExcludeLootFromBoost = data == null ? null : data.get("excludeLootFromBoost");

		if (rawExcludeLootFromBoost instanceof Boolean bool) {
			return bool;
		}

		LOGGER.warn("Missing or invalid 'excludeLootFromBoost' in {}, using default of {}",
				configFile, DEFAULT_EXCLUDE_LOOT_FROM_BOOST);
		return DEFAULT_EXCLUDE_LOOT_FROM_BOOST;
	}

	private static int clamp(int value, int min, int max, String fieldName) {
		int clamped = Math.max(min, Math.min(value, max));
		if (clamped != value) {
			LOGGER.warn("Configured {} {} is out of range ({}-{}), clamping to {}", fieldName, value, min, max, clamped);
		}
		return clamped;
	}
}
