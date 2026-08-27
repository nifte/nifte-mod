package dev.nifte.feature.chat;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;

public final class ChatAvatars {
	private static @Nullable UUID pendingUuid;
	private static @Nullable GameProfile pendingProfile;

	private ChatAvatars() {
	}

	public static void captureSender(UUID uuid, GameProfile profile) {
		pendingUuid = uuid;
		pendingProfile = profile;
	}

	public static void clearSender() {
		pendingUuid = null;
		pendingProfile = null;
	}

	public static Component decorate(Component message) {
		UUID uuid = pendingUuid;
		GameProfile profile = pendingProfile;
		clearSender();
		if (!NifteConfig.get().chatAvatarsEnabled || ChatAvatarSprite.hasPlayerSprite(message)) {
			return message;
		}

		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		PlayerInfo player = ChatAvatarPlayers.fromTabList(connection, uuid, profile);
		if (player != null) {
			return withAvatar(message, player);
		}

		if (profile != null) {
			return ChatAvatarSprite.prepend(message, ResolvableProfile.createResolved(profile), true);
		}

		if (uuid != null && !Util.NIL_UUID.equals(uuid)) {
			return ChatAvatarSprite.prepend(message, ResolvableProfile.createUnresolved(uuid), true);
		}

		player = ChatAvatarPlayers.findMentioned(connection, message);
		if (player != null) {
			return withAvatar(message, player);
		}

		return message;
	}

	private static Component withAvatar(Component message, PlayerInfo player) {
		return ChatAvatarSprite.prepend(
			message,
			ResolvableProfile.createResolved(player.getProfile()),
			player.showHat()
		);
	}
}
