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
			""";

	private static volatile int maxLevel = DEFAULT_MAX_LEVEL;
	private static volatile int maxBookshelves = DEFAULT_MAX_BOOKSHELVES;

	private ModConfig() {
	}

	public static int getMaxLevel() {
		return maxLevel;
	}

	public static int getMaxBookshelves() {
		return maxBookshelves;
	}

	public static void load() {
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve("config.yaml");

		if (!Files.exists(configFile)) {
			writeDefault(configFile);
			maxLevel = DEFAULT_MAX_LEVEL;
			maxBookshelves = DEFAULT_MAX_BOOKSHELVES;
		} else {
			Map<?, ?> data = readYaml(configFile);
			maxLevel = readMaxLevel(configFile, data);
			maxBookshelves = readMaxBookshelves(configFile, data);
		}

		LOGGER.info("Unlimited Enchants: maxLevel set to {}, maxBookshelves set to {}", maxLevel, maxBookshelves);
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

	private static int clamp(int value, int min, int max, String fieldName) {
		int clamped = Math.max(min, Math.min(value, max));
		if (clamped != value) {
			LOGGER.warn("Configured {} {} is out of range ({}-{}), clamping to {}", fieldName, value, min, max, clamped);
		}
		return clamped;
	}
}
