package dev.nifte.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import dev.nifte.Nifte;

public final class NifteConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("nifte.json");
	private static NifteConfig instance = new NifteConfig();

	public boolean fpsEnabled = true;
	public HudAnchor fpsAnchor = HudAnchor.TOP_LEFT;
	public int fpsOffsetX = 2;
	public int fpsOffsetY = 2;
	public float fpsScale = 1.0F;
	public int fpsColor = 0xFFFFFF;
	public boolean fpsHideWithDebug = true;

	public boolean armorHudEnabled = true;
	public ArmorHudAnchor armorAnchor = ArmorHudAnchor.BOTTOM_LEFT;
	public int armorOffsetX = 2;
	public int armorOffsetY = 2;
	public float armorScale = 1.0F;

	public boolean potionHudEnabled = true;

	public boolean numericalPing = true;

	public boolean disableFrontThirdPerson = true;

	public boolean zoomEnabled = true;
	public boolean zoomToggleMode = false;
	public float zoomFov = 30.0F;

	public boolean fullbrightEnabled = false;

	public boolean guiMoveEnabled = false;

	public DropConfirmMode dropConfirmMode = DropConfirmMode.DISABLED;
	public int dropConfirmSeconds = 3;

	public boolean itemScrollEnabled = false;
	public boolean containerSortEnabled = false;
	public boolean containerDragEnabled = false;

	public boolean autoTotemEnabled = true;
	public boolean autoElytraEnabled = false;
	public boolean autoToolEnabled = false;
	public boolean autoToolFromInventory = false;
	public boolean autoWeaponEnabled = false;
	public boolean autoWeaponFromInventory = false;
	public boolean toolProtectEnabled = true;
	public boolean bedrockBridgingEnabled = false;
	public boolean fastBlockPlacementEnabled = false;
	public boolean handRestockEnabled = false;
	public boolean noBreakDelayEnabled = false;

	public boolean unlockAllRecipes = true;
	public boolean recipeBookScrollEnabled = true;
	public boolean keepCraftingCentered = true;
	public boolean instantCraftEnabled = false;

	public boolean disableAdvancementToasts = false;
	public boolean disableRecipeToasts = false;
	public boolean foodHungerNameEnabled = true;
	public boolean shulkerBoxTooltipEnabled = true;

	public List<String> disabledParticles = new ArrayList<>();

	private NifteConfig() {
	}

	public static NifteConfig get() {
		return instance;
	}

	public static void load() {
		if (!Files.exists(PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(PATH)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			if (json.has("dropConfirmMode") && "TOOLS_WEAPONS_OR_ENCHANTED".equals(json.get("dropConfirmMode").getAsString())) {
				json.addProperty("dropConfirmMode", "TOOLS_AND_WEAPONS");
			}

			NifteConfig loaded = GSON.fromJson(json, NifteConfig.class);
			if (loaded != null) {
				if (loaded.armorAnchor == null) {
					loaded.armorAnchor = ArmorHudAnchor.BOTTOM_LEFT;
				}

				if (loaded.dropConfirmMode == null) {
					loaded.dropConfirmMode = DropConfirmMode.DISABLED;
				}

				if (json.has("dropConfirmEnabled") && !json.get("dropConfirmEnabled").getAsBoolean()) {
					loaded.dropConfirmMode = DropConfirmMode.DISABLED;
				}

				if (!json.has("dropConfirmSeconds") && json.has("dropConfirmTicks")) {
					loaded.dropConfirmSeconds = Math.max(1, Math.round(json.get("dropConfirmTicks").getAsInt() / 20.0F));
				}

				if (loaded.dropConfirmSeconds < 1) {
					loaded.dropConfirmSeconds = 3;
				} else if (loaded.dropConfirmSeconds > 10) {
					loaded.dropConfirmSeconds = 10;
				}

				instance = loaded;
			}
		} catch (IOException exception) {
			Nifte.LOGGER.warn("Failed to read config, using defaults", exception);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException exception) {
			Nifte.LOGGER.warn("Failed to write config", exception);
		}
	}

	public boolean isParticleDisabled(Identifier id) {
		return this.disabledParticles.contains(id.toString());
	}

	public void setParticleDisabled(Identifier id, boolean disabled) {
		String key = id.toString();
		if (disabled) {
			if (!this.disabledParticles.contains(key)) {
				this.disabledParticles.add(key);
			}
		} else {
			this.disabledParticles.remove(key);
		}
	}
}
