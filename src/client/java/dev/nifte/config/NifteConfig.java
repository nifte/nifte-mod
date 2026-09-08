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
import net.minecraft.util.Mth;

import dev.nifte.Nifte;
import dev.nifte.feature.glow.EntityGlowFeature;
import dev.nifte.hud.PlayerTracers;

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
	public boolean armorShowHeldItems = true;

	public boolean potionHudEnabled = true;

	public boolean mobHealthEnabled = true;
	public HudAnchor mobHealthAnchor = HudAnchor.TOP_RIGHT;
	public int mobHealthOffsetX = 2;
	public int mobHealthOffsetY = 2;
	public float mobHealthScale = 1.0F;
	public int mobHealthFadeSeconds = 3;
	public int mobHealthReach = 5;

	public boolean compassHudEnabled = true;
	public float compassScale = 1.0F;

	public boolean numericalPing = true;
	public boolean chatAvatarsEnabled = true;

	public boolean disableFrontThirdPerson = true;
	public boolean autoThirdPerson = false;
	public boolean dynamicThirdPerson = false;

	public boolean zoomEnabled = true;
	public float zoomFov = 30.0F;
	public ZoomTransition zoomTransition = ZoomTransition.SMOOTH;

	public boolean fullbrightEnabled = false;
	public boolean disableFog = false;

	public boolean lowerFireOverlay = true;
	public float fireOverlayOffset = 0.35F;
	public boolean lowerShield = true;
	public float shieldOffset = 0.3F;
	public boolean hideHeldTotem = false;

	public boolean guiMoveEnabled = false;
	public boolean keepSprintOnWallEnabled = false;

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
	public ToolProtectMode toolProtectMode = ToolProtectMode.DISABLED;
	public boolean bedrockBridgingEnabled = false;
	public boolean fastBlockPlacementEnabled = false;
	public boolean blockRestockEnabled = false;
	public boolean bucketRestockEnabled = false;
	public boolean consumableRestockEnabled = false;
	public boolean noBreakDelayEnabled = false;

	public boolean unlockAllRecipes = true;
	public boolean recipeBookScrollEnabled = true;
	public boolean keepCraftingCentered = true;
	public boolean instantCraftEnabled = false;

	public boolean disableAdvancementToasts = false;
	public boolean disableRecipeToasts = false;
	public boolean foodHungerNameEnabled = true;
	public boolean shulkerBoxTooltipEnabled = true;

	public boolean hideDeadMobs = false;
	public boolean highlightHostileMobs = false;
	public int highlightHostileMobsRange = EntityGlowFeature.RANGE_DEFAULT;
	public boolean highlightOtherPlayers = false;
	public int highlightOtherPlayersRange = EntityGlowFeature.RANGE_DEFAULT;
	public boolean playerTracersEnabled = false;
	public int playerTracersRange = PlayerTracers.RANGE_DEFAULT;
	public boolean ignoreGrassInCombat = false;
	public boolean quickEatEnabled = true;
	public boolean quickUseEnabled = true;
	public boolean projectileTrajectoryEnabled = true;

	public boolean hideFirstPersonEffectParticles = false;
	public List<String> disabledParticles = new ArrayList<>();

	public String lastConfigCategory = "nifte.config.hud";

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

				if (loaded.mobHealthAnchor == null) {
					loaded.mobHealthAnchor = HudAnchor.TOP_RIGHT;
				}

				if (loaded.mobHealthFadeSeconds < 0) {
					loaded.mobHealthFadeSeconds = 3;
				} else if (loaded.mobHealthFadeSeconds > 10) {
					loaded.mobHealthFadeSeconds = 10;
				}

				loaded.mobHealthReach = Mth.clamp(loaded.mobHealthReach, 1, 20);
				loaded.highlightHostileMobsRange = Mth.clamp(loaded.highlightHostileMobsRange, EntityGlowFeature.RANGE_MIN, EntityGlowFeature.RANGE_MAX);
				loaded.highlightOtherPlayersRange = Mth.clamp(loaded.highlightOtherPlayersRange, EntityGlowFeature.RANGE_MIN, EntityGlowFeature.RANGE_MAX);
				loaded.playerTracersRange = Mth.clamp(loaded.playerTracersRange, PlayerTracers.RANGE_MIN, PlayerTracers.RANGE_MAX);

				if (loaded.dropConfirmMode == null) {
					loaded.dropConfirmMode = DropConfirmMode.DISABLED;
				}

				if (json.has("dropConfirmEnabled") && !json.get("dropConfirmEnabled").getAsBoolean()) {
					loaded.dropConfirmMode = DropConfirmMode.DISABLED;
				}

				if (loaded.zoomTransition == null) {
					loaded.zoomTransition = ZoomTransition.SMOOTH;
				}

				if (loaded.toolProtectMode == null) {
					loaded.toolProtectMode = ToolProtectMode.DISABLED;
				}

				if (json.has("toolProtectEnabled") && !json.has("toolProtectMode")) {
					loaded.toolProtectMode = json.get("toolProtectEnabled").getAsBoolean()
						? ToolProtectMode.ALL_TOOLS
						: ToolProtectMode.DISABLED;
				}

				if (!json.has("dropConfirmSeconds") && json.has("dropConfirmTicks")) {
					loaded.dropConfirmSeconds = Math.max(1, Math.round(json.get("dropConfirmTicks").getAsInt() / 20.0F));
				}

				if (loaded.dropConfirmSeconds < 1) {
					loaded.dropConfirmSeconds = 3;
				} else if (loaded.dropConfirmSeconds > 10) {
					loaded.dropConfirmSeconds = 10;
				}

				if (!json.has("quickEatEnabled") && json.has("fastEatingEnabled")) {
					loaded.quickEatEnabled = json.get("fastEatingEnabled").getAsBoolean();
				}

				if (!json.has("highlightHostileMobs") && json.has("glowHostileMobs")) {
					loaded.highlightHostileMobs = json.get("glowHostileMobs").getAsBoolean();
				}

				if (!json.has("highlightOtherPlayers") && json.has("glowOtherPlayers")) {
					loaded.highlightOtherPlayers = json.get("glowOtherPlayers").getAsBoolean();
				}

				if (json.has("handRestockEnabled") && !json.has("blockRestockEnabled") && !json.has("bucketRestockEnabled")) {
					boolean enabled = json.get("handRestockEnabled").getAsBoolean();
					loaded.blockRestockEnabled = enabled;
					loaded.bucketRestockEnabled = enabled;
				}

				loaded.fireOverlayOffset = Mth.clamp(loaded.fireOverlayOffset, 0.0F, 0.6F);
				loaded.shieldOffset = Mth.clamp(loaded.shieldOffset, 0.0F, 0.8F);

				instance = loaded;
			}
		} catch (IOException exception) {
			Nifte.LOGGER.warn("Failed to read config, using defaults", exception);
		}
	}

	public static void save() {
		if (NifteConfigPreview.isOpen()) {
			return;
		}

		write();
	}

	NifteConfig copy() {
		NifteConfig copy = GSON.fromJson(GSON.toJson(this), NifteConfig.class);
		if (copy.disabledParticles == null) {
			copy.disabledParticles = new ArrayList<>();
		} else {
			copy.disabledParticles = new ArrayList<>(copy.disabledParticles);
		}

		return copy;
	}

	static void restore(NifteConfig snapshot) {
		instance = snapshot.copy();
	}

	static void write() {
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
