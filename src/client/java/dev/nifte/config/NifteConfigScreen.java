package dev.nifte.config;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import dev.nifte.feature.particles.ParticleGroups;

public final class NifteConfigScreen {
	private static final Function<Boolean, Component> ENABLED_DISABLED = value -> Component.translatable(
		value ? "nifte.config.enabled" : "nifte.config.disabled"
	).withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
	private static final Function<Boolean, Component> ZOOM_HOLD_TOGGLE = value -> Component.translatable(
		value ? "nifte.config.zoom.mode.toggle" : "nifte.config.zoom.mode.hold"
	).withStyle(ChatFormatting.WHITE);
	private static final int HUD_SCALE_MIN = 5;
	private static final int HUD_SCALE_MAX = 30;
	private static final int HUD_SCALE_DEFAULT = 10;

	private NifteConfigScreen() {
	}

	public static Screen create(Screen parent) {
		NifteConfig config = NifteConfig.get();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.translatable("nifte.config.title"))
			.setSavingRunnable(NifteConfig::save);
		ConfigEntryBuilder entries = builder.entryBuilder();

		addHudCategory(builder, entries, config);
		addCameraCategory(builder, entries, config);
		addInventoryCategory(builder, entries, config);
		addEquipmentCategory(builder, entries, config);
		addBuildingCategory(builder, entries, config);
		addCraftingCategory(builder, entries, config);
		addUiCategory(builder, entries, config);
		addParticlesCategory(builder, entries, config);
		return builder.build();
	}

	private static void addHudCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.hud"));
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.fps"), config.fpsEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.fpsEnabled = value)
			.build());
		addAnchor(entries, category, "nifte.config.fps.anchor", config.fpsAnchor, HudAnchor.TOP_LEFT, value -> config.fpsAnchor = value);
		addOffset(entries, category, "nifte.config.fps.x", config.fpsOffsetX, 2, value -> config.fpsOffsetX = value);
		addOffset(entries, category, "nifte.config.fps.y", config.fpsOffsetY, 2, value -> config.fpsOffsetY = value);
		addScale(entries, category, "nifte.config.fps.scale", config.fpsScale, value -> config.fpsScale = value);
		addEntry(category, entries.startColorField(Component.translatable("nifte.config.fps.color"), config.fpsColor)
			.setDefaultValue(0xFFFFFF)
			.setSaveConsumer(value -> config.fpsColor = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.fps.hide_debug"), config.fpsHideWithDebug)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.fpsHideWithDebug = value)
			.build());

		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.armor"), config.armorHudEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.armorHudEnabled = value)
			.build());
		addEntry(category, entries.startEnumSelector(Component.translatable("nifte.config.armor.anchor"), ArmorHudAnchor.class, config.armorAnchor)
			.setDefaultValue(ArmorHudAnchor.BOTTOM_LEFT)
			.setSaveConsumer(value -> config.armorAnchor = value)
			.build());
		addOffset(entries, category, "nifte.config.armor.x", config.armorOffsetX, 2, value -> config.armorOffsetX = value);
		addOffset(entries, category, "nifte.config.armor.y", config.armorOffsetY, 2, value -> config.armorOffsetY = value);
		addScale(entries, category, "nifte.config.armor.scale", config.armorScale, value -> config.armorScale = value);

		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.potion"), config.potionHudEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.potionHudEnabled = value)
			.build());
	}

	private static void addCameraCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.camera"));
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.front_third_person"), config.disableFrontThirdPerson)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.disableFrontThirdPerson = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.zoom"), config.zoomEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.zoomEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, Component.translatable("nifte.config.zoom.mode"), config.zoomToggleMode, ZOOM_HOLD_TOGGLE)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.zoomToggleMode = value)
			.build());
		int zoomFov = Mth.clamp(Math.round(config.zoomFov), 10, 70);
		addEntry(category, entries.startIntSlider(Component.translatable("nifte.config.zoom.fov"), zoomFov, 10, 70)
			.setDefaultValue(30)
			.setSaveConsumer(value -> config.zoomFov = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.fullbright"), config.fullbrightEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.fullbrightEnabled = value)
			.build());
	}

	private static void addInventoryCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.inventory"));
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.item_scroll"), config.itemScrollEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.itemScrollEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.sort"), config.containerSortEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.containerSortEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.container_drag"), config.containerDragEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.containerDragEnabled = value)
			.build());
		addEntry(category, entries.startEnumSelector(Component.translatable("nifte.config.drop_confirm"), DropConfirmMode.class, config.dropConfirmMode)
			.setDefaultValue(DropConfirmMode.DISABLED)
			.setEnumNameProvider(mode -> ((DropConfirmMode) mode).optionLabel())
			.setSaveConsumer(value -> config.dropConfirmMode = value)
			.build());
		addEntry(category, entries.startIntSlider(Component.translatable("nifte.config.drop_confirm.seconds"), config.dropConfirmSeconds, 1, 10)
			.setDefaultValue(3)
			.setTextGetter(value -> Component.translatable("nifte.config.drop_confirm.seconds.value", value))
			.setSaveConsumer(value -> config.dropConfirmSeconds = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.gui_move"), config.guiMoveEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.guiMoveEnabled = value)
			.build());
	}

	private static void addEquipmentCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.equipment"));
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.auto_totem"), config.autoTotemEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.autoTotemEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.auto_elytra"), config.autoElytraEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.autoElytraEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.auto_tool"), config.autoToolEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.autoToolEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.auto_tool.inventory"), config.autoToolFromInventory)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.autoToolFromInventory = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.auto_weapon"), config.autoWeaponEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.autoWeaponEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.auto_weapon.inventory"), config.autoWeaponFromInventory)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.autoWeaponFromInventory = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.tool_protect"), config.toolProtectEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.toolProtectEnabled = value)
			.build());
	}

	private static void addBuildingCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.building"));
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.bedrock_bridging"), config.bedrockBridgingEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.bedrockBridgingEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.fast_block_placement"), config.fastBlockPlacementEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.fastBlockPlacementEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.hand_restock"), config.handRestockEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.handRestockEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.no_break_delay"), config.noBreakDelayEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.noBreakDelayEnabled = value)
			.build());
	}

	private static void addCraftingCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.crafting"));
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.unlock_recipes"), config.unlockAllRecipes)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.unlockAllRecipes = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.recipe_scroll"), config.recipeBookScrollEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.recipeBookScrollEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.recipe_centered"), config.keepCraftingCentered)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.keepCraftingCentered = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.instant_craft"), config.instantCraftEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.instantCraftEnabled = value)
			.build());
	}

	private static void addUiCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.ui"));
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.ping"), config.numericalPing)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.numericalPing = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.food_hunger"), config.foodHungerNameEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.foodHungerNameEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.shulker_tooltip"), config.shulkerBoxTooltipEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.shulkerBoxTooltipEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.toasts.advancement"), config.disableAdvancementToasts)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.disableAdvancementToasts = value)
			.build());
		addEntry(category, booleanToggle(entries,Component.translatable("nifte.config.toasts.recipe"), config.disableRecipeToasts)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.disableRecipeToasts = value)
			.build());
	}

	private static void addParticlesCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.particles"));
		addEntry(category, entries.startTextDescription(Component.translatable("nifte.config.particles.list")).build());
		ParticleGroups.grouped().forEach((group, ids) -> {
			if (ids.isEmpty()) {
				return;
			}

			SubCategoryBuilder list = entries.startSubCategory(Component.translatable(group.langKey()));
			list.setExpanded(false);
			for (Identifier id : ids) {
				boolean enabled = !config.isParticleDisabled(id);
				list.add(booleanToggle(entries, Component.literal(ParticleGroups.label(id)), enabled)
					.setDefaultValue(true)
					.setSaveConsumer(value -> config.setParticleDisabled(id, !value))
					.build());
			}

			addEntry(category, list.build());
		});
	}

	private static BooleanToggleBuilder booleanToggle(ConfigEntryBuilder entries, Component name, boolean value) {
		return booleanToggle(entries, name, value, ENABLED_DISABLED);
	}

	private static BooleanToggleBuilder booleanToggle(
		ConfigEntryBuilder entries,
		Component name,
		boolean value,
		Function<Boolean, Component> yesNoText
	) {
		return entries.startBooleanToggle(name, value).setYesNoTextSupplier(yesNoText);
	}

	private static void addAnchor(
		ConfigEntryBuilder entries,
		ConfigCategory category,
		String key,
		HudAnchor current,
		HudAnchor defaultValue,
		Consumer<HudAnchor> saver
	) {
		addEntry(category, entries.startEnumSelector(Component.translatable(key), HudAnchor.class, current)
			.setDefaultValue(defaultValue)
			.setSaveConsumer(saver)
			.build());
	}

	private static void addScale(
		ConfigEntryBuilder entries,
		ConfigCategory category,
		String key,
		float current,
		Consumer<Float> saver
	) {
		int slider = Mth.clamp(Math.round(current * 10.0F), HUD_SCALE_MIN, HUD_SCALE_MAX);
		addEntry(category, entries.startIntSlider(Component.translatable(key), slider, HUD_SCALE_MIN, HUD_SCALE_MAX)
			.setDefaultValue(HUD_SCALE_DEFAULT)
			.setTextGetter(NifteConfigScreen::scaleLabel)
			.setSaveConsumer(value -> saver.accept(value / 10.0F))
			.build());
	}

	private static Component scaleLabel(int tenths) {
		return Component.translatable("nifte.config.hud.scale.value", String.format(Locale.ROOT, "%.1f", tenths / 10.0F));
	}

	private static void addOffset(
		ConfigEntryBuilder entries,
		ConfigCategory category,
		String key,
		int current,
		int defaultValue,
		Consumer<Integer> saver
	) {
		addEntry(category, entries.startIntSlider(Component.translatable(key), current, 0, 400)
			.setDefaultValue(defaultValue)
			.setSaveConsumer(saver)
			.build());
	}

	private static void addEntry(ConfigCategory category, AbstractConfigListEntry<?> entry) {
		category.addEntry(entry);
	}

	public static boolean isNifteScreen(Screen screen) {
		if (screen == null) {
			return false;
		}

		if (!(screen.getTitle().getContents() instanceof TranslatableContents contents)) {
			return false;
		}

		return "nifte.config.title".equals(contents.getKey());
	}
}
