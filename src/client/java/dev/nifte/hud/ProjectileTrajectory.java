package dev.nifte.hud;

import java.util.List;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.hud.ProjectileTrajectoryPath.Flight;

public final class ProjectileTrajectory {
	private static final int COLOR = ARGB.color(255, 255, 255);
	private static final float LINE_WIDTH = 3.0F;
	private static final float END_DOT_SIZE = 8.0F;

	private ProjectileTrajectory() {
	}

	public static void emit(Minecraft minecraft, Camera camera, float partialTick) {
		if (!NifteConfig.get().projectileTrajectoryEnabled || minecraft.gui.hud.isHidden()) {
			return;
		}

		if (!minecraft.options.getCameraType().isFirstPerson()) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null || player.isSpectator()) {
			return;
		}

		Shot shot = resolve(player, partialTick);
		if (shot == null) {
			return;
		}

		List<Vec3> physics = ProjectileTrajectoryPath.trace(minecraft.level, player, shot.physics());
		if (physics.size() < 2) {
			return;
		}

		Vec3 visualStart = ProjectileTrajectoryOrigin.visual(minecraft, camera, player, shot.hand(), shot.stack(), partialTick);
		List<Vec3> points = ProjectileTrajectoryPath.fromHand(physics, visualStart);
		Vec3 cameraPos = camera.position();
		List<AABB> occluders = HudLineOcclusion.occluderBoxes(minecraft.level, player, cameraPos, points, partialTick);
		for (List<Vec3> span : HudLineOcclusion.visibleSpans(minecraft.level, cameraPos, points, occluders)) {
			HudLines.polyline(minecraft, camera, span, COLOR, LINE_WIDTH, true);
		}

		Vec3 impact = points.getLast();
		if (HudLineOcclusion.visible(minecraft.level, cameraPos, impact, occluders)) {
			Gizmos.point(impact, COLOR, END_DOT_SIZE).setAlwaysOnTop();
		}
	}

	private static @Nullable Shot resolve(LocalPlayer player, float partialTick) {
		if (player.isUsingItem()) {
			Shot using = aimedShot(player, player.getUsedItemHand(), player.getUseItem(), partialTick);
			if (using != null) {
				return using;
			}
		}

		for (InteractionHand hand : InteractionHand.values()) {
			Shot aimed = aimedShot(player, hand, player.getItemInHand(hand), partialTick);
			if (aimed != null) {
				return aimed;
			}
		}

		for (InteractionHand hand : InteractionHand.values()) {
			Shot thrown = thrownShot(player, hand, player.getItemInHand(hand), partialTick);
			if (thrown != null) {
				return thrown;
			}
		}

		return null;
	}

	private static @Nullable Shot aimedShot(LocalPlayer player, InteractionHand hand, ItemStack stack, float partialTick) {
		Item item = stack.getItem();
		if (item instanceof BowItem) {
			if (!isUsing(player, stack)) {
				return null;
			}

			float power = BowItem.getPowerForTime(player.getTicksUsingItem());
			if (power < 0.1F || !hasBowAmmo(player, stack)) {
				return null;
			}

			return arrowShot(player, hand, stack, partialTick, power * 3.0F, 0.6F);
		}

		if (item instanceof TridentItem) {
			if (!isUsing(player, stack)) {
				return null;
			}

			if (EnchantmentHelper.getTridentSpinAttackStrength(stack, player) > 0.0F) {
				return null;
			}

			return arrowShot(player, hand, stack, partialTick, TridentItem.PROJECTILE_SHOOT_POWER, 0.99F);
		}

		if (item instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
			ChargedProjectiles charged = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
			if (charged.contains(Items.FIREWORK_ROCKET)) {
				return null;
			}

			return arrowShot(player, hand, stack, partialTick, 3.15F, 0.6F);
		}

		return null;
	}

	private static @Nullable Shot thrownShot(LocalPlayer player, InteractionHand hand, ItemStack stack, float partialTick) {
		Item item = stack.getItem();
		if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderpearlItem) {
			return throwableShot(player, hand, stack, partialTick, SnowballItem.PROJECTILE_SHOOT_POWER, 0.0F, 0.03);
		}

		if (item instanceof ThrowablePotionItem) {
			return throwableShot(player, hand, stack, partialTick, ThrowablePotionItem.PROJECTILE_SHOOT_POWER, -20.0F, 0.05);
		}

		if (item instanceof ExperienceBottleItem) {
			return throwableShot(player, hand, stack, partialTick, 0.7F, -20.0F, 0.07);
		}

		if (item instanceof WindChargeItem) {
			Vec3 origin = ProjectileTrajectoryOrigin.spawn(player, partialTick);
			return new Shot(Flight.wind(origin, launchVelocity(player, partialTick, WindChargeItem.PROJECTILE_SHOOT_POWER, 0.0F)), hand, stack);
		}

		return null;
	}

	private static Shot throwableShot(
		LocalPlayer player,
		InteractionHand hand,
		ItemStack stack,
		float partialTick,
		float power,
		float pitchOffset,
		double gravity
	) {
		Vec3 origin = ProjectileTrajectoryOrigin.spawn(player, partialTick);
		return new Shot(Flight.throwable(origin, launchVelocity(player, partialTick, power, pitchOffset), gravity), hand, stack);
	}

	private static Shot arrowShot(
		LocalPlayer player,
		InteractionHand hand,
		ItemStack stack,
		float partialTick,
		float power,
		float waterDrag
	) {
		Vec3 origin = ProjectileTrajectoryOrigin.spawn(player, partialTick);
		return new Shot(Flight.arrow(origin, launchVelocity(player, partialTick, power, 0.0F), waterDrag), hand, stack);
	}

	private static Vec3 launchVelocity(LocalPlayer player, float partialTick, float power, float pitchOffset) {
		float xRot = player.getXRot(partialTick);
		float yRot = player.getYRot(partialTick);
		float deg = (float) (Math.PI / 180.0);
		float xd = -Mth.sin(yRot * deg) * Mth.cos(xRot * deg);
		float yd = -Mth.sin((xRot + pitchOffset) * deg);
		float zd = Mth.cos(yRot * deg) * Mth.cos(xRot * deg);
		Vec3 velocity = new Vec3(xd, yd, zd).normalize().scale(power);
		Vec3 movement = player.getKnownMovement();
		return velocity.add(movement.x, player.onGround() ? 0.0 : movement.y, movement.z);
	}

	private static boolean isUsing(LocalPlayer player, ItemStack stack) {
		return player.isUsingItem() && player.getUseItem().getItem() == stack.getItem();
	}

	private static boolean hasBowAmmo(LocalPlayer player, ItemStack bow) {
		return player.hasInfiniteMaterials() || !player.getProjectile(bow).isEmpty();
	}

	private record Shot(Flight physics, InteractionHand hand, ItemStack stack) {
	}
}
