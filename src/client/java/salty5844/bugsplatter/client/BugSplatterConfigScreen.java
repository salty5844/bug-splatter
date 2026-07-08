package salty5844.bugsplatter.client;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public final class BugSplatterConfigScreen extends Screen {

	public BugSplatterConfigScreen(Screen parent) {
		super(Component.literal("Bug Splatter Settings"));
	}

	@Override
	protected void init() {
		BugSplatterConfig config = BugSplatterConfig.getInstance();
		AtomicReference<CycleButton<Boolean>> realisticBugsToggleRef = new AtomicReference<>();
		int centerX = this.width / 2;
		String titleText = "Bug Splatter Settings";
		int titleWidth = this.font.width(titleText);
		this.addRenderableWidget(new net.minecraft.client.gui.components.StringWidget((this.width - titleWidth) / 2, 12, titleWidth, 20, Component.literal(titleText), this.font));
		int top = 64;
		int rowHeight = 24;

		int index = 0;
		for (String effectKey : config.getEffects().keySet()) {
			int rowY = top + index * rowHeight;
			String label = formatEffectName(effectKey);
			if ("bug_splatter".equals(effectKey)) {
				label = "Bug splatters";
			} else if ("realistic_bugs".equals(effectKey)) {
				label = "Realistic bugs";
			}
			final String displayLabel = Objects.requireNonNull(label);
			int labelWidth = this.font.width(displayLabel);
			int buttonWidth = 120;
			int rowWidth = labelWidth + 8 + buttonWidth;
			int rowX = centerX - (rowWidth / 2);
			int labelX = rowX;
			int buttonX = rowX + labelWidth + 8;
			CycleButton<Boolean> toggle = CycleButton.onOffBuilder(config.isEnabled(effectKey))
				.displayOnlyValue()
				.withTooltip(value -> Tooltip.create(Component.literal(value ? "Enabled" : "Disabled")))
				.create(buttonX, rowY, buttonWidth, 20, Component.empty(), (button, value) -> {
					config.setEnabled(effectKey, value);
					if ("bug_splatter".equals(effectKey)) {
						CycleButton<Boolean> realisticBugsToggle = realisticBugsToggleRef.get();
						if (realisticBugsToggle != null) {
							realisticBugsToggle.active = value;
						}
					}
				});
			if ("realistic_bugs".equals(effectKey)) {
				toggle.active = config.isEnabled("bug_splatter");
				realisticBugsToggleRef.set(toggle);
			}
			this.addRenderableWidget(new net.minecraft.client.gui.components.StringWidget(labelX, rowY, labelWidth, 20, Component.literal(displayLabel), this.font));
			this.addRenderableWidget(toggle);
			index++;
		}

		this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
			Path configDir = FabricLoader.getInstance().getConfigDir();
			BugSplatterConfig.getInstance().save(configDir);
			this.onClose();
		}).bounds(centerX - 100, this.height - 28, 200, 20).build());
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, 0xCC10131A);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	private static String formatEffectName(String key) {
		String[] parts = key.split("_");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.toString();
	}
}