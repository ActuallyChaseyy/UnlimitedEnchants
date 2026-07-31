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
	private static final int DEFAULT_MAX_LEVEL = 100;
	// Matches Enchantment.MAX_LEVEL, the absolute ceiling the game's data format allows.
	private static final int HARD_CEILING = 255;

	private static final String DEFAULT_CONTENT = """
			# Unlimited Enchants configuration
			#
			# maxLevel controls the highest level any enchantment can reach, via the
			# enchanting table, anvil combining, commands (/enchant), and loot.
			# Vanilla enchantments normally cap out around 4-5. Must be between 1 and 255.
			maxLevel: 100
			""";

	private static volatile int maxLevel = DEFAULT_MAX_LEVEL;

	private ModConfig() {
	}

	public static int getMaxLevel() {
		return maxLevel;
	}

	public static void load() {
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve("config.yaml");

		if (!Files.exists(configFile)) {
			writeDefault(configFile);
			maxLevel = DEFAULT_MAX_LEVEL;
		} else {
			maxLevel = readMaxLevel(configFile);
		}

		LOGGER.info("Unlimited Enchants: maxLevel set to {}", maxLevel);
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

	private static int readMaxLevel(Path configFile) {
		try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
			Map<?, ?> data = new Yaml().load(reader);
			Object rawMaxLevel = data == null ? null : data.get("maxLevel");

			if (rawMaxLevel instanceof Number number) {
				return clamp(number.intValue());
			}

			LOGGER.warn("Missing or invalid 'maxLevel' in {}, using default of {}", configFile, DEFAULT_MAX_LEVEL);
		} catch (IOException e) {
			LOGGER.error("Failed to read config from {}, using default of {}", configFile, DEFAULT_MAX_LEVEL, e);
		} catch (RuntimeException e) {
			// SnakeYAML throws unchecked exceptions (e.g. YAMLException) on malformed YAML.
			LOGGER.error("Malformed YAML in {}, using default of {}", configFile, DEFAULT_MAX_LEVEL, e);
		}

		return DEFAULT_MAX_LEVEL;
	}

	private static int clamp(int value) {
		int clamped = Math.max(1, Math.min(value, HARD_CEILING));
		if (clamped != value) {
			LOGGER.warn("Configured maxLevel {} is out of range (1-{}), clamping to {}", value, HARD_CEILING, clamped);
		}
		return clamped;
	}
}
