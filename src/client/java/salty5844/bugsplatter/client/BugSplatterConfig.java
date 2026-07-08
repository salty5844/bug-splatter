package salty5844.bugsplatter.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BugSplatterConfig {

	private static final String FILE_NAME = "bug-splatter.json";
	private static final BugSplatterConfig INSTANCE = new BugSplatterConfig();

	private final Map<String, Boolean> effectEnabled = new LinkedHashMap<>();

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

	public void load(Path configDirectory) {
		Path file = configDirectory.resolve(FILE_NAME);
		if (!Files.exists(file)) {
			return;
		}

		try {
			for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
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
				effectEnabled.put(key, Boolean.parseBoolean(value));
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to load Bug Splatter config", exception);
		}
	}

	public void save(Path configDirectory) {
		Path file = configDirectory.resolve(FILE_NAME);
		try {
			Files.createDirectories(configDirectory);
			StringBuilder builder = new StringBuilder();
			builder.append("# Bug Splatter config\n");
			builder.append("# Toggle individual particle effects on or off.\n");
			for (Map.Entry<String, Boolean> entry : effectEnabled.entrySet()) {
				builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
			}
			Files.writeString(file, builder.toString(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to save Bug Splatter config", exception);
		}
	}
}