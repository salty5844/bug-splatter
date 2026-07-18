package salty5844.bugsplatter.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BugSplatterConfig {

	private static final String FILE_NAME = "bug-splatter.json";
	private static final BugSplatterConfig INSTANCE = new BugSplatterConfig();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final int MIN_GROUND_PROXIMITY = 1;
	private static final int MAX_GROUND_PROXIMITY = 100;
	private static final double MIN_BIOME_MULTIPLIER = 0.0D;
	private static final double MAX_BIOME_MULTIPLIER = 10.0D;
	private static final int DEFAULT_REGULAR_GROUND_PROXIMITY = 10;
	private static final int DEFAULT_DOUBLE_GROUND_PROXIMITY = 5;
	private static final boolean DEFAULT_THIRD_PERSON_SPLATS = false;
	private static final double DEFAULT_SWAMP_MULTIPLIER = 1.5D;
	private static final double DEFAULT_JUNGLE_MULTIPLIER = 1.5D;

	private final Map<String, Boolean> effectEnabled = new LinkedHashMap<>();
	private int regularGroundProximity = DEFAULT_REGULAR_GROUND_PROXIMITY;
	private int doubleGroundProximity = DEFAULT_DOUBLE_GROUND_PROXIMITY;
	private boolean thirdPersonSplats = DEFAULT_THIRD_PERSON_SPLATS;
	private double swampMultiplier = DEFAULT_SWAMP_MULTIPLIER;
	private double jungleMultiplier = DEFAULT_JUNGLE_MULTIPLIER;

	private BugSplatterConfig() {
		effectEnabled.put("bug_splatter", true);
		effectEnabled.put("realistic_bugs", true);
	}

	public static BugSplatterConfig getInstance() {
		return INSTANCE;
	}

	public boolean isEnabled(String key) {
		return effectEnabled.getOrDefault(key, true);
	}

	public void setEnabled(String key, boolean enabled) {
		effectEnabled.put(key, enabled);
	}

	public Map<String, Boolean> getEffects() {
		return effectEnabled;
	}

	public int getRegularGroundProximity() {
		return regularGroundProximity;
	}

	public void setRegularGroundProximity(int value) {
		regularGroundProximity = clampInt(value, MIN_GROUND_PROXIMITY, MAX_GROUND_PROXIMITY);
	}

	public int getDoubleGroundProximity() {
		return doubleGroundProximity;
	}

	public void setDoubleGroundProximity(int value) {
		doubleGroundProximity = clampInt(value, MIN_GROUND_PROXIMITY, MAX_GROUND_PROXIMITY);
	}

	public boolean isThirdPersonSplatsEnabled() {
		return thirdPersonSplats;
	}

	public void setThirdPersonSplatsEnabled(boolean enabled) {
		thirdPersonSplats = enabled;
	}

	public double getSwampMultiplier() {
		return swampMultiplier;
	}

	public void setSwampMultiplier(double value) {
		swampMultiplier = clampDouble(value, MIN_BIOME_MULTIPLIER, MAX_BIOME_MULTIPLIER);
	}

	public double getJungleMultiplier() {
		return jungleMultiplier;
	}

	public void setJungleMultiplier(double value) {
		jungleMultiplier = clampDouble(value, MIN_BIOME_MULTIPLIER, MAX_BIOME_MULTIPLIER);
	}

	public void resetToDefaults() {
		effectEnabled.clear();
		effectEnabled.put("bug_splatter", true);
		effectEnabled.put("realistic_bugs", true);
		regularGroundProximity = DEFAULT_REGULAR_GROUND_PROXIMITY;
		doubleGroundProximity = DEFAULT_DOUBLE_GROUND_PROXIMITY;
		thirdPersonSplats = DEFAULT_THIRD_PERSON_SPLATS;
		swampMultiplier = DEFAULT_SWAMP_MULTIPLIER;
		jungleMultiplier = DEFAULT_JUNGLE_MULTIPLIER;
	}

	public void load(Path configDirectory) {
		Path file = configDirectory.resolve(FILE_NAME);
		if (!Files.exists(file)) {
			return;
		}

		try {
			String content = Files.readString(file, StandardCharsets.UTF_8).trim();
			if (content.isEmpty()) {
				return;
			}
			if (content.startsWith("{")) {
				loadFromJson(content);
			} else {
				loadLegacy(content);
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to load Bug Splatter config", exception);
		}
	}

	public void save(Path configDirectory) {
		Path file = configDirectory.resolve(FILE_NAME);
		try {
			Files.createDirectories(configDirectory);
			JsonObject root = new JsonObject();
			root.addProperty("bug_splatter", isEnabled("bug_splatter"));
			root.addProperty("realistic_bugs", isEnabled("realistic_bugs"));
			root.addProperty("regular_ground_proximity", regularGroundProximity);
			root.addProperty("double_ground_proximity", doubleGroundProximity);
			root.addProperty("third_person_splats", thirdPersonSplats);
			root.addProperty("swamp_multiplier", swampMultiplier);
			root.addProperty("jungle_multiplier", jungleMultiplier);
			Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to save Bug Splatter config", exception);
		}
	}

	private void loadFromJson(String content) {
		try {
			JsonElement element = JsonParser.parseString(content);
			if (!element.isJsonObject()) {
				return;
			}
			JsonObject root = element.getAsJsonObject();
			if (root.has("bug_splatter")) {
				setEnabled("bug_splatter", root.get("bug_splatter").getAsBoolean());
			}
			if (root.has("realistic_bugs")) {
				setEnabled("realistic_bugs", root.get("realistic_bugs").getAsBoolean());
			}
			if (root.has("regular_ground_proximity")) {
				setRegularGroundProximity(root.get("regular_ground_proximity").getAsInt());
			}
			if (root.has("double_ground_proximity")) {
				setDoubleGroundProximity(root.get("double_ground_proximity").getAsInt());
			}
			if (root.has("third_person_splats")) {
				setThirdPersonSplatsEnabled(root.get("third_person_splats").getAsBoolean());
			}
			if (root.has("swamp_multiplier")) {
				setSwampMultiplier(root.get("swamp_multiplier").getAsDouble());
			}
			if (root.has("jungle_multiplier")) {
				setJungleMultiplier(root.get("jungle_multiplier").getAsDouble());
			}
		} catch (ClassCastException | IllegalStateException | UnsupportedOperationException | JsonParseException ignored) {
			// Invalid JSON falls back to current in-memory defaults.
		}
	}

	private void loadLegacy(String content) {
		for (String line : content.split("\\R")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			int separator = trimmed.indexOf('=');
			if (separator <= 0) {
				continue;
			}
			String key = trimmed.substring(0, separator).trim();
			String value = trimmed.substring(separator + 1).trim();
			switch (key) {
				case "bug_splatter", "realistic_bugs" -> effectEnabled.put(key, Boolean.parseBoolean(value));
				case "regular_ground_proximity" -> setRegularGroundProximity(parseIntOrDefault(value, regularGroundProximity));
				case "double_ground_proximity" -> setDoubleGroundProximity(parseIntOrDefault(value, doubleGroundProximity));
				case "third_person_splats" -> setThirdPersonSplatsEnabled(Boolean.parseBoolean(value));
				case "swamp_multiplier" -> setSwampMultiplier(parseDoubleOrDefault(value, swampMultiplier));
				case "jungle_multiplier" -> setJungleMultiplier(parseDoubleOrDefault(value, jungleMultiplier));
				default -> {
				}
			}
		}
	}

	private static int parseIntOrDefault(String raw, int fallback) {
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException ignored) {
			return fallback;
		}
	}

	private static double parseDoubleOrDefault(String raw, double fallback) {
		try {
			return Double.parseDouble(raw);
		} catch (NumberFormatException ignored) {
			return fallback;
		}
	}

	private static int clampInt(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static double clampDouble(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}