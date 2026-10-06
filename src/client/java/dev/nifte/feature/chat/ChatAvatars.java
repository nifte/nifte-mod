package dev.nifte.feature.chat;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;

import org.jspecify.annotations.Nullable;

import dev.nifte.config.NifteConfig;
import dev.nifte.mixin.ChatComponentAccessor;

public final class ChatAvatars {
	private static final int RECENT_JOIN_TICKS = 40;

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

		UUID hovered = ChatAvatarPlayers.firstHoveredPlayer(message);
		if (hovered != null && connection != null) {
			player = connection.getPlayerInfo(hovered);
			if (player != null) {
				return withAvatar(message, player);
			}
		}

		player = ChatAvatarPlayers.findMentioned(connection, message);
		if (player != null) {
			return withAvatar(message, player);
		}

		return message;
	}

	public static void attachListedPlayer(PlayerInfo player) {
		if (!NifteConfig.get().chatAvatarsEnabled) {
			return;
		}

		// Join lines arrive before the tab-list packet that adds the player, so the
		// avatar has to be applied when that packet lands.
		Minecraft minecraft = Minecraft.getInstance();
		ChatComponentAccessor chat = (ChatComponentAccessor) minecraft.gui.hud.getChat();
		List<GuiMessage> messages = chat.nifte$allMessages();
		int now = minecraft.gui.hud.getGuiTicks();
		boolean changed = false;
		for (int index = 0; index < messages.size(); index++) {
			GuiMessage line = messages.get(index);
			int age = now - line.addedTime();
			if (age < 0 || age > RECENT_JOIN_TICKS || ChatAvatarSprite.hasPlayerSprite(line.content())) {
				continue;
			}

			if (!ChatAvatarPlayers.mentions(player, line.content(), minecraft.getConnection())) {
				continue;
			}

			messages.set(
				index,
				new GuiMessage(line.addedTime(), withAvatar(line.content(), player), line.signature(), line.source(), line.tag())
			);
			changed = true;
		}

		if (!changed) {
			return;
		}

		int scroll = chat.nifte$getChatScrollbarPos();
		boolean pendingScrollNotice = chat.nifte$isNewMessageSinceScroll();
		chat.nifte$refreshTrimmedMessages();
		chat.nifte$setChatScrollbarPos(scroll);
		chat.nifte$setNewMessageSinceScroll(pendingScrollNotice);
	}

	private static Component withAvatar(Component message, PlayerInfo player) {
		return ChatAvatarSprite.prepend(
			message,
			ResolvableProfile.createResolved(player.getProfile()),
			player.showHat()
		);
	}
}
