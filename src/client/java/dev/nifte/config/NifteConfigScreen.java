package dev.nifte.config;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import dev.nifte.feature.overlay.FireOverlayFeature;
import dev.nifte.feature.overlay.ShieldOverlayFeature;
import dev.nifte.feature.particles.ParticleGroups;
import dev.nifte.input.NifteKeybinds;

public final class NifteConfigScreen {
	private static final Function<Boolean, Component> ENABLED_DISABLED = value -> Component.translatable(
		value ? "nifte.config.enabled" : "nifte.config.disabled"
	).withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
	private static final int HUD_SCALE_MIN = 5;
	private static final int HUD_SCALE_MAX = 30;
	private static final int HUD_SCALE_DEFAULT = 10;

	private NifteConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ConfigKeybindEditor.clear();
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
		addCombatCategory(builder, entries, config);
		addBuildingCategory(builder, entries, config);
		addCraftingCategory(builder, entries, config);
		addUiCategory(builder, entries, config);
		addParticlesCategory(builder, entries, config);
		return builder.build();
	}

	private static void addHudCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.hud"));
		addEntry(category, booleanToggle(entries, "nifte.config.fps", config.fpsEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.fpsEnabled = value)
			.build());
		addSettings(
			category,
			entries,
			"nifte.config.fps",
			anchor(entries, "nifte.config.fps.anchor", config.fpsAnchor, HudAnchor.TOP_LEFT, value -> config.fpsAnchor = value),
			offset(entries, "nifte.config.fps.x", config.fpsOffsetX, 2, value -> config.fpsOffsetX = value),
			offset(entries, "nifte.config.fps.y", config.fpsOffsetY, 2, value -> config.fpsOffsetY = value),
			scale(entries, "nifte.config.fps.scale", config.fpsScale, value -> config.fpsScale = value),
			entries.startColorField(Component.translatable("nifte.config.fps.color"), config.fpsColor)
				.setDefaultValue(0xFFFFFF)
				.setSaveConsumer(value -> config.fpsColor = value)
				.build(),
			booleanToggle(entries, "nifte.config.fps.hide_debug", config.fpsHideWithDebug)
				.setDefaultValue(true)
				.setSaveConsumer(value -> config.fpsHideWithDebug = value)
				.build()
		);

		addEntry(category, booleanToggle(entries, "nifte.config.armor", config.armorHudEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.armorHudEnabled = value)
			.build());
		addSettings(
			category,
			entries,
			"nifte.config.armor",
			entries.startEnumSelector(Component.translatable("nifte.config.armor.anchor"), ArmorHudAnchor.class, config.armorAnchor)
				.setDefaultValue(ArmorHudAnchor.BOTTOM_LEFT)
				.setSaveConsumer(value -> config.armorAnchor = value)
				.build(),
			offset(entries, "nifte.config.armor.x", config.armorOffsetX, 2, value -> config.armorOffsetX = value),
			offset(entries, "nifte.config.armor.y", config.armorOffsetY, 2, value -> config.armorOffsetY = value),
			scale(entries, "nifte.config.armor.scale", config.armorScale, value -> config.armorScale = value),
			booleanToggle(entries, "nifte.config.armor.held", config.armorShowHeldItems)
				.setDefaultValue(true)
				.setSaveConsumer(value -> config.armorShowHeldItems = value)
				.build()
		);

		addEntry(category, booleanToggle(entries, "nifte.config.potion", config.potionHudEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.potionHudEnabled = value)
			.build());
	}

	private static void addCameraCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.camera"));
		addEntry(category, booleanToggle(entries, "nifte.config.front_third_person", config.disableFrontThirdPerson)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.disableFrontThirdPerson = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.auto_third_person", config.autoThirdPerson)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.auto_third_person"))
			.setSaveConsumer(value -> config.autoThirdPerson = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.dynamic_third_person", config.dynamicThirdPerson)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.dynamic_third_person"))
			.setSaveConsumer(value -> config.dynamicThirdPerson = value)
			.build());
		addEntry(category, keybindToggle(
			entries,
			"nifte.config.zoom",
			config.zoomEnabled,
			true,
			NifteKeybinds.zoom,
			value -> config.zoomEnabled = value,
			"key.nifte.zoom"
		));
		int zoomFov = Mth.clamp(Math.round(config.zoomFov), 10, 70);
		addSettings(
			category,
			entries,
			"nifte.config.zoom",
			entries.startIntSlider(Component.translatable("nifte.config.zoom.fov"), zoomFov, 10, 70)
				.setDefaultValue(30)
				.setTextGetter(value -> Component.translatable("nifte.config.zoom.fov.value", value))
				.setTooltip(tooltip("nifte.config.zoom.fov"))
				.setSaveConsumer(value -> config.zoomFov = value)
				.build()
		);
		addEntry(category, keybindToggle(
			entries,
			"nifte.config.fullbright",
			config.fullbrightEnabled,
			false,
			NifteKeybinds.fullbright,
			value -> config.fullbrightEnabled = value
		));
		addEntry(category, booleanToggle(entries, "nifte.config.disable_fog", config.disableFog)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.disable_fog"))
			.setSaveConsumer(value -> config.disableFog = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.lower_fire", config.lowerFireOverlay)
			.setDefaultValue(true)
			.setTooltip(tooltip("nifte.config.lower_fire"))
			.setSaveConsumer(value -> config.lowerFireOverlay = value)
			.build());
		addSettings(
			category,
			entries,
			"nifte.config.lower_fire",
			overlayOffset(
				entries,
				"nifte.config.lower_fire.offset",
				config.fireOverlayOffset,
				FireOverlayFeature.DEFAULT_OFFSET,
				FireOverlayFeature.MAX_OFFSET,
				value -> config.fireOverlayOffset = value
			)
		);
		addEntry(category, booleanToggle(entries, "nifte.config.lower_shield", config.lowerShield)
			.setDefaultValue(true)
			.setTooltip(tooltip("nifte.config.lower_shield"))
			.setSaveConsumer(value -> config.lowerShield = value)
			.build());
		addSettings(
			category,
			entries,
			"nifte.config.lower_shield",
			overlayOffset(
				entries,
				"nifte.config.lower_shield.offset",
				config.shieldOffset,
				ShieldOverlayFeature.DEFAULT_OFFSET,
				ShieldOverlayFeature.MAX_OFFSET,
				value -> config.shieldOffset = value
			)
		);
		addEntry(category, booleanToggle(entries, "nifte.config.hide_held_totem", config.hideHeldTotem)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.hide_held_totem"))
			.setSaveConsumer(value -> config.hideHeldTotem = value)
			.build());
	}

	private static void addInventoryCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.inventory"));
		addEntry(category, booleanToggle(entries, "nifte.config.item_scroll", config.itemScrollEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.itemScrollEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.sort", config.containerSortEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.containerSortEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.container_drag", config.containerDragEnabled)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.container_drag"))
			.setSaveConsumer(value -> config.containerDragEnabled = value)
			.build());
		addEntry(category, entries.startEnumSelector(Component.translatable("nifte.config.drop_confirm"), DropConfirmMode.class, config.dropConfirmMode)
			.setDefaultValue(DropConfirmMode.DISABLED)
			.setEnumNameProvider(mode -> ((DropConfirmMode) mode).optionLabel())
			.setTooltip(tooltip("nifte.config.drop_confirm"))
			.setSaveConsumer(value -> config.dropConfirmMode = value)
			.build());
		addSettings(
			category,
			entries,
			"nifte.config.drop_confirm",
			entries.startIntSlider(Component.translatable("nifte.config.drop_confirm.seconds"), config.dropConfirmSeconds, 1, 10)
				.setDefaultValue(3)
				.setTextGetter(NifteConfigScreen::dropConfirmSecondsLabel)
				.setSaveConsumer(value -> config.dropConfirmSeconds = value)
				.build()
		);
		addEntry(category, booleanToggle(entries, "nifte.config.gui_move", config.guiMoveEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.guiMoveEnabled = value)
			.build());
		addEntry(category, keybindToggle(
			entries,
			"nifte.config.quick_eat",
			config.quickEatEnabled,
			true,
			NifteKeybinds.quickEat,
			value -> config.quickEatEnabled = value,
			"key.nifte.quick_eat"
		));
		addEntry(category, booleanToggle(entries, "nifte.config.quick_use", config.quickUseEnabled)
			.setDefaultValue(true)
			.setTooltip(tooltip("nifte.config.quick_use"))
			.setSaveConsumer(value -> config.quickUseEnabled = value)
			.build());
		AbstractConfigListEntry<?>[] quickUseSlots = new AbstractConfigListEntry<?>[NifteKeybinds.quickUseSlots.length];
		for (int slot = 0; slot < NifteKeybinds.quickUseSlots.length; slot++) {
			quickUseSlots[slot] = new KeybindFieldEntry(
				Component.translatable("key.nifte.quick_use." + (slot + 1)),
				NifteKeybinds.quickUseSlots[slot]
			);
		}
		addSettings(category, entries, "nifte.config.quick_use", quickUseSlots);
	}

	private static void addEquipmentCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.equipment"));
		addEntry(category, booleanToggle(entries, "nifte.config.auto_totem", config.autoTotemEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.autoTotemEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.auto_elytra", config.autoElytraEnabled)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.auto_elytra"))
			.setSaveConsumer(value -> config.autoElytraEnabled = value)
			.build());
		addEntry(category, keybindToggle(
			entries,
			"nifte.config.auto_tool",
			config.autoToolEnabled,
			false,
			NifteKeybinds.autoTool,
			value -> config.autoToolEnabled = value
		));
		addSettings(
			category,
			entries,
			"nifte.config.auto_tool",
			booleanToggle(entries, "nifte.config.auto_tool.inventory", config.autoToolFromInventory)
				.setDefaultValue(false)
				.setSaveConsumer(value -> config.autoToolFromInventory = value)
				.build()
		);
		addEntry(category, entries.startEnumSelector(Component.translatable("nifte.config.tool_protect"), ToolProtectMode.class, config.toolProtectMode)
			.setDefaultValue(ToolProtectMode.ALL_TOOLS)
			.setEnumNameProvider(mode -> ((ToolProtectMode) mode).optionLabel())
			.setSaveConsumer(value -> config.toolProtectMode = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.bucket_restock", config.bucketRestockEnabled)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.bucket_restock"))
			.setSaveConsumer(value -> config.bucketRestockEnabled = value)
			.build());
	}

	private static void addCombatCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.combat"));
		addEntry(category, keybindToggle(
			entries,
			"nifte.config.auto_weapon",
			config.autoWeaponEnabled,
			false,
			NifteKeybinds.autoWeapon,
			value -> config.autoWeaponEnabled = value
		));
		addSettings(
			category,
			entries,
			"nifte.config.auto_weapon",
			booleanToggle(entries, "nifte.config.auto_weapon.inventory", config.autoWeaponFromInventory)
				.setDefaultValue(false)
				.setSaveConsumer(value -> config.autoWeaponFromInventory = value)
				.build()
		);
		addEntry(category, booleanToggle(entries, "nifte.config.hide_dead_mobs", config.hideDeadMobs)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.hideDeadMobs = value)
			.build());
		addEntry(category, keybindToggle(
			entries,
			"nifte.config.highlight_hostile_mobs",
			config.highlightHostileMobs,
			false,
			NifteKeybinds.highlightHostileMobs,
			value -> config.highlightHostileMobs = value,
			"nifte.config.highlight_hostile_mobs"
		));
		addEntry(category, keybindToggle(
			entries,
			"nifte.config.highlight_other_players",
			config.highlightOtherPlayers,
			false,
			NifteKeybinds.highlightOtherPlayers,
			value -> config.highlightOtherPlayers = value,
			"nifte.config.highlight_other_players"
		));
		addEntry(category, booleanToggle(entries, "nifte.config.trajectory", config.projectileTrajectoryEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.projectileTrajectoryEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.ignore_grass", config.ignoreGrassInCombat)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.ignore_grass"))
			.setSaveConsumer(value -> config.ignoreGrassInCombat = value)
			.build());
	}

	private static void addBuildingCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.building"));
		addEntry(category, booleanToggle(entries, "nifte.config.bedrock_bridging", config.bedrockBridgingEnabled)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.bedrock_bridging"))
			.setSaveConsumer(value -> config.bedrockBridgingEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.fast_block_placement", config.fastBlockPlacementEnabled)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.fast_block_placement"))
			.setSaveConsumer(value -> config.fastBlockPlacementEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.block_restock", config.blockRestockEnabled)
			.setDefaultValue(false)
			.setTooltip(tooltip("nifte.config.block_restock"))
			.setSaveConsumer(value -> config.blockRestockEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.no_break_delay", config.noBreakDelayEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.noBreakDelayEnabled = value)
			.build());
	}

	private static void addCraftingCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.crafting"));
		addEntry(category, booleanToggle(entries, "nifte.config.unlock_recipes", config.unlockAllRecipes)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.unlockAllRecipes = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.recipe_scroll", config.recipeBookScrollEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.recipeBookScrollEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.recipe_centered", config.keepCraftingCentered)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.keepCraftingCentered = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.instant_craft", config.instantCraftEnabled)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.instantCraftEnabled = value)
			.build());
	}

	private static void addUiCategory(ConfigBuilder builder, ConfigEntryBuilder entries, NifteConfig config) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("nifte.config.ui"));
		addEntry(category, booleanToggle(entries, "nifte.config.chat_avatars", config.chatAvatarsEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.chatAvatarsEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.ping", config.numericalPing)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.numericalPing = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.food_hunger", config.foodHungerNameEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.foodHungerNameEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.shulker_tooltip", config.shulkerBoxTooltipEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(value -> config.shulkerBoxTooltipEnabled = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.toasts.advancement", config.disableAdvancementToasts)
			.setDefaultValue(false)
			.setSaveConsumer(value -> config.disableAdvancementToasts = value)
			.build());
		addEntry(category, booleanToggle(entries, "nifte.config.toasts.recipe", config.disableRecipeToasts)
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

	private static BooleanToggleBuilder booleanToggle(ConfigEntryBuilder entries, String key, boolean value) {
		return booleanToggle(entries, Component.translatable(key), value, ENABLED_DISABLED);
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

	private static AbstractConfigListEntry<?> anchor(
		ConfigEntryBuilder entries,
		String key,
		HudAnchor current,
		HudAnchor defaultValue,
		Consumer<HudAnchor> saver
	) {
		return entries.startEnumSelector(Component.translatable(key), HudAnchor.class, current)
			.setDefaultValue(defaultValue)
			.setSaveConsumer(saver)
			.build();
	}

	private static AbstractConfigListEntry<?> scale(
		ConfigEntryBuilder entries,
		String key,
		float current,
		Consumer<Float> saver
	) {
		int slider = Mth.clamp(Math.round(current * 10.0F), HUD_SCALE_MIN, HUD_SCALE_MAX);
		return entries.startIntSlider(Component.translatable(key), slider, HUD_SCALE_MIN, HUD_SCALE_MAX)
			.setDefaultValue(HUD_SCALE_DEFAULT)
			.setTextGetter(NifteConfigScreen::scaleLabel)
			.setSaveConsumer(value -> saver.accept(value / 10.0F))
			.build();
	}

	private static Component scaleLabel(int tenths) {
		return Component.translatable("nifte.config.hud.scale.value", String.format(Locale.ROOT, "%.1f", tenths / 10.0F));
	}

	private static Component offsetLabel(int pixels) {
		return Component.translatable("nifte.config.hud.offset.value", pixels);
	}

	private static AbstractConfigListEntry<?> offset(
		ConfigEntryBuilder entries,
		String key,
		int current,
		int defaultValue,
		Consumer<Integer> saver
	) {
		return entries.startIntSlider(Component.translatable(key), current, 0, 400)
			.setDefaultValue(defaultValue)
			.setTextGetter(NifteConfigScreen::offsetLabel)
			.setSaveConsumer(saver)
			.build();
	}

	private static AbstractConfigListEntry<?> overlayOffset(
		ConfigEntryBuilder entries,
		String key,
		float current,
		float defaultValue,
		float max,
		Consumer<Float> saver
	) {
		int maxSlider = Math.round(max * 100.0F);
		int slider = Mth.clamp(Math.round(current * 100.0F), 0, maxSlider);
		return entries.startIntSlider(Component.translatable(key), slider, 0, maxSlider)
			.setDefaultValue(Math.round(defaultValue * 100.0F))
			.setTextGetter(NifteConfigScreen::overlayOffsetLabel)
			.setSaveConsumer(value -> saver.accept(value / 100.0F))
			.build();
	}

	private static Component dropConfirmSecondsLabel(int seconds) {
		String key = seconds == 1
			? "nifte.config.drop_confirm.seconds.value.singular"
			: "nifte.config.drop_confirm.seconds.value";
		return Component.translatable(key, seconds);
	}

	private static Component overlayOffsetLabel(int hundredths) {
		return Component.translatable(
			"nifte.config.overlay.offset.value",
			String.format(Locale.ROOT, "%.2f", hundredths / 100.0F)
		);
	}

	private static Component tooltip(String key) {
		return Component.translatable(key + ".tooltip");
	}

	private static BooleanListEntry keybindToggle(
		ConfigEntryBuilder entries,
		String key,
		boolean value,
		boolean defaultValue,
		KeyMapping mapping,
		Consumer<Boolean> saver
	) {
		return keybindToggle(entries, key, value, defaultValue, mapping, saver, null);
	}

	private static BooleanListEntry keybindToggle(
		ConfigEntryBuilder entries,
		String key,
		boolean value,
		boolean defaultValue,
		KeyMapping mapping,
		Consumer<Boolean> saver,
		String tooltipKey
	) {
		KeybindToggleEntry entry = new KeybindToggleEntry(
			Component.translatable(key),
			value,
			entries.getResetButtonKey(),
			() -> defaultValue,
			saver,
			mapping,
			ENABLED_DISABLED
		);
		if (tooltipKey != null) {
			entry.setTooltipSupplier(() -> Optional.of(new Component[] { tooltip(tooltipKey) }));
		}

		return entry;
	}

	private static void addSettings(
		ConfigCategory category,
		ConfigEntryBuilder entries,
		String featureKey,
		AbstractConfigListEntry<?>... children
	) {
		SubCategoryBuilder list = entries.startSubCategory(Component.translatable(featureKey + ".settings"));
		list.setExpanded(false);
		for (AbstractConfigListEntry<?> child : children) {
			list.add(child);
		}

		addEntry(category, list.build());
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
