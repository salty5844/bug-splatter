package salty5844.bugsplatter.client;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class BugSplatterConfigScreen extends Screen {

	private static final int ROW_HEIGHT = 24;
	private static final int LABEL_WIDTH = 200;
	private static final int VALUE_BOX_WIDTH = 50;
	private static final int SLIDER_WIDTH = 150;
	private static final int TOGGLE_WIDTH = 120;

	public BugSplatterConfigScreen(Screen parent) {
		super(Component.literal("Bug Splatter Settings"));
		Objects.requireNonNull(parent);
	}

	@Override
	protected void init() {
		BugSplatterConfig config = BugSplatterConfig.getInstance();
		AtomicReference<CycleButton<Boolean>> realisticBugsToggleRef = new AtomicReference<>();
		List<AbstractWidget> dependentWidgets = new ArrayList<>();

		String titleText = "Bug Splatter Settings";
		int titleWidth = this.font.width(titleText);
		this.addRenderableWidget(new StringWidget((this.width - titleWidth) / 2, 12, titleWidth, 20, Component.literal(titleText), this.font));
		this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
			config.resetToDefaults();
			this.clearWidgets();
			this.init();
		}).bounds(this.width - 70, 12, 56, 20).build());

		int centerX = this.width / 2;
		int contentX = centerX - ((LABEL_WIDTH + 8 + TOGGLE_WIDTH) / 2);
		int rowY = 48;

		CycleButton<Boolean> bugSplattersToggle = addToggleRow(
			"Bug splatters",
			config.isEnabled("bug_splatter"),
			contentX,
			rowY,
			value -> {
				config.setEnabled("bug_splatter", value);
				CycleButton<Boolean> realisticBugsToggle = realisticBugsToggleRef.get();
				if (realisticBugsToggle != null) {
					realisticBugsToggle.active = value;
				}
				updateDependentWidgetsActive(dependentWidgets, value);
			}
		);
		rowY += ROW_HEIGHT;

		CycleButton<Boolean> realisticBugsToggle = addToggleRow(
			"Realistic bugs",
			config.isEnabled("realistic_bugs"),
			contentX,
			rowY,
			value -> config.setEnabled("realistic_bugs", value)
		);
		realisticBugsToggle.active = bugSplattersToggle.getValue();
		realisticBugsToggleRef.set(realisticBugsToggle);
		dependentWidgets.add(realisticBugsToggle);
		rowY += ROW_HEIGHT;

		CycleButton<Boolean> thirdPersonToggle = addToggleRow(
			"Third person splats",
			config.isThirdPersonSplatsEnabled(),
			contentX,
			rowY,
			config::setThirdPersonSplatsEnabled
		);
		dependentWidgets.add(thirdPersonToggle);
		rowY += ROW_HEIGHT + 4;

		addIntegerSliderRow(
			"Regular ground proximity",
			contentX,
			rowY,
			1,
			100,
			config::getRegularGroundProximity,
			config::setRegularGroundProximity,
			dependentWidgets
		);
		rowY += ROW_HEIGHT;

		addIntegerSliderRow(
			"Double-rate ground proximity",
			contentX,
			rowY,
			1,
			100,
			config::getDoubleGroundProximity,
			config::setDoubleGroundProximity,
			dependentWidgets
		);
		rowY += ROW_HEIGHT;

		addDecimalSliderRow(
			"Swamp multiplier",
			contentX,
			rowY,
			0.0D,
			10.0D,
			1,
			config::getSwampMultiplier,
			config::setSwampMultiplier,
			dependentWidgets
		);
		rowY += ROW_HEIGHT;

		addDecimalSliderRow(
			"Jungle multiplier",
			contentX,
			rowY,
			0.0D,
			10.0D,
			1,
			config::getJungleMultiplier,
			config::setJungleMultiplier,
			dependentWidgets
		);

		updateDependentWidgetsActive(dependentWidgets, bugSplattersToggle.getValue());

		this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
			Path configDir = FabricLoader.getInstance().getConfigDir();
			BugSplatterConfig.getInstance().save(configDir);
			this.onClose();
		}).bounds(centerX - 100, this.height - 28, 200, 20).build());
	}

	private CycleButton<Boolean> addToggleRow(String label, boolean initialValue, int rowX, int rowY, BooleanValueConsumer onChange) {
		@NonNull String displayLabel = requireNonNullString(label);
		this.addRenderableWidget(new StringWidget(rowX, rowY, LABEL_WIDTH, 20, Component.literal(displayLabel), this.font));
		CycleButton<Boolean> toggle = CycleButton.onOffBuilder(initialValue)
			.displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.literal(Boolean.TRUE.equals(value) ? "Enabled" : "Disabled")))
			.create(rowX + LABEL_WIDTH + 8, rowY, TOGGLE_WIDTH, 20, Component.empty(), (button, value) -> onChange.accept(Boolean.TRUE.equals(value)));
		this.addRenderableWidget(toggle);
		return toggle;
	}

	private void addIntegerSliderRow(
		String label,
		int rowX,
		int rowY,
		int min,
		int max,
		IntSupplier getter,
		IntConsumer setter,
		List<AbstractWidget> dependentWidgets
	) {
		@NonNull String displayLabel = requireNonNullString(label);
		this.addRenderableWidget(new StringWidget(rowX, rowY, LABEL_WIDTH, 20, Component.literal(displayLabel), this.font));
		EditBox valueBox = new EditBox(this.font, rowX + LABEL_WIDTH + 8, rowY, VALUE_BOX_WIDTH, 20, Component.literal(displayLabel));
		IntegerSlider slider = new IntegerSlider(
			rowX + LABEL_WIDTH + 8 + VALUE_BOX_WIDTH + 6,
			rowY,
			SLIDER_WIDTH,
			20,
			min,
			max,
			getter.getAsInt(),
			value -> {
				setter.accept(value);
				valueBox.setValue(requireNonNullString(Integer.toString(value)));
			}
		);
		valueBox.setValue(requireNonNullString(Integer.toString(getter.getAsInt())));
		valueBox.setResponder(text -> {
			if (text == null || text.isBlank()) {
				return;
			}
			try {
				int parsed = Integer.parseInt(text);
				int clamped = Math.max(min, Math.min(max, parsed));
				setter.accept(clamped);
				slider.setIntValue(clamped);
				if (clamped != parsed) {
					valueBox.setValue(requireNonNullString(Integer.toString(clamped)));
				}
			} catch (NumberFormatException ignored) {
			}
		});
		this.addRenderableWidget(valueBox);
		this.addRenderableWidget(slider);
		dependentWidgets.add(valueBox);
		dependentWidgets.add(slider);
	}

	private void addDecimalSliderRow(
		String label,
		int rowX,
		int rowY,
		double min,
		double max,
		int precision,
		DoubleSupplier getter,
		DoubleConsumer setter,
		List<AbstractWidget> dependentWidgets
	) {
		@NonNull String displayLabel = requireNonNullString(label);
		this.addRenderableWidget(new StringWidget(rowX, rowY, LABEL_WIDTH, 20, Component.literal(displayLabel), this.font));
		EditBox valueBox = new EditBox(this.font, rowX + LABEL_WIDTH + 8, rowY, VALUE_BOX_WIDTH, 20, Component.literal(displayLabel));
		DecimalSlider slider = new DecimalSlider(
			rowX + LABEL_WIDTH + 8 + VALUE_BOX_WIDTH + 6,
			rowY,
			SLIDER_WIDTH,
			20,
			min,
			max,
			precision,
			getter.getAsDouble(),
			value -> {
				setter.accept(value);
				valueBox.setValue(requireNonNullString(formatDecimal(value, precision)));
			}
		);
		valueBox.setValue(requireNonNullString(formatDecimal(getter.getAsDouble(), precision)));
		valueBox.setResponder(text -> {
			if (text == null || text.isBlank()) {
				return;
			}
			try {
				double parsed = Double.parseDouble(text);
				double clamped = Math.max(min, Math.min(max, parsed));
				double normalized = roundToPrecision(clamped, precision);
				setter.accept(normalized);
				slider.setDoubleValue(normalized);
				if (Math.abs(parsed - normalized) > 0.0000001D) {
					valueBox.setValue(requireNonNullString(formatDecimal(normalized, precision)));
				}
			} catch (NumberFormatException ignored) {
			}
		});
		this.addRenderableWidget(valueBox);
		this.addRenderableWidget(slider);
		dependentWidgets.add(valueBox);
		dependentWidgets.add(slider);
	}

	private void updateDependentWidgetsActive(List<AbstractWidget> widgets, boolean enabled) {
		for (AbstractWidget widget : widgets) {
			widget.active = enabled;
		}
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

	private static @NonNull String formatDecimal(double value, int precision) {
		return requireNonNullString(String.format(Locale.ROOT, "%1$." + precision + "f", roundToPrecision(value, precision)));
	}

	private static double roundToPrecision(double value, int precision) {
		double scale = Math.pow(10.0D, precision);
		return Math.round(value * scale) / scale;
	}

	private static final class IntegerSlider extends AbstractSliderButton {

		private final int min;
		private final int max;
		private final IntConsumer onValueChanged;

		private IntegerSlider(int x, int y, int width, int height, int min, int max, int initialValue, IntConsumer onValueChanged) {
			super(x, y, width, height, Component.empty(), 0.0D);
			this.min = min;
			this.max = max;
			this.onValueChanged = onValueChanged;
			setIntValue(initialValue);
		}

		private int getIntValue() {
			return (int) Math.round(min + this.value * (max - min));
		}

		private void setIntValue(int intValue) {
			int clamped = Math.max(min, Math.min(max, intValue));
			if (max == min) {
				this.value = 0.0D;
			} else {
				this.value = (double) (clamped - min) / (double) (max - min);
			}
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Component.literal(requireNonNullString(Integer.toString(getIntValue()))));
		}

		@Override
		protected void applyValue() {
			int current = getIntValue();
			setIntValue(current);
			onValueChanged.accept(current);
		}
	}

	private static final class DecimalSlider extends AbstractSliderButton {

		private final double min;
		private final double max;
		private final int precision;
		private final DoubleConsumer onValueChanged;

		private DecimalSlider(int x, int y, int width, int height, double min, double max, int precision, double initialValue, DoubleConsumer onValueChanged) {
			super(x, y, width, height, Component.empty(), 0.0D);
			this.min = min;
			this.max = max;
			this.precision = precision;
			this.onValueChanged = onValueChanged;
			setDoubleValue(initialValue);
		}

		private double getDoubleValue() {
			return roundToPrecision(min + this.value * (max - min), precision);
		}

		private void setDoubleValue(double doubleValue) {
			double clamped = Math.max(min, Math.min(max, doubleValue));
			if (Math.abs(max - min) < 0.0000001D) {
				this.value = 0.0D;
			} else {
				this.value = (clamped - min) / (max - min);
			}
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Component.literal(requireNonNullString(formatDecimal(getDoubleValue(), precision))));
		}

		@Override
		protected void applyValue() {
			double current = getDoubleValue();
			setDoubleValue(current);
			onValueChanged.accept(current);
		}
	}

	@FunctionalInterface
	private interface BooleanValueConsumer {
		void accept(boolean value);
	}

	private static @NonNull String requireNonNullString(@Nullable String value) {
		if (value == null) {
			return "";
		}
		return value;
	}
}
