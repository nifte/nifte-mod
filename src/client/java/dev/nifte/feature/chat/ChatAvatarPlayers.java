package dev.nifte.feature.chat;

import java.util.UUID;
import java.util.regex.Pattern;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import org.jspecify.annotations.Nullable;

final class ChatAvatarPlayers {
	private static final Pattern FORMATTING = Pattern.compile("§.");
	private static final int MIN_NAME_LENGTH = 2;

	private ChatAvatarPlayers() {
	}

	static @Nullable PlayerInfo fromTabList(
		@Nullable ClientPacketListener connection,
		@Nullable UUID uuid,
		@Nullable GameProfile profile
	) {
		if (connection == null) {
			return null;
		}

		if (uuid != null && !Util.NIL_UUID.equals(uuid)) {
			PlayerInfo byId = connection.getPlayerInfo(uuid);
			if (byId != null) {
				return byId;
			}
		}

		if (profile != null) {
			return connection.getPlayerInfo(profile.name());
		}

		return null;
	}

	static @Nullable PlayerInfo findMentioned(@Nullable ClientPacketListener connection, Component message) {
		if (connection == null) {
			return null;
		}

		return findMentioned(connection, message.getString());
	}

	private static @Nullable PlayerInfo findMentioned(ClientPacketListener connection, String text) {
		if (text.isEmpty()) {
			return null;
		}

		PlayerInfo best = null;
		int bestIndex = Integer.MAX_VALUE;
		int bestLength = 0;
		for (PlayerInfo info : connection.getOnlinePlayers()) {
			for (String name : namesOf(info)) {
				int index = indexOfName(text, name);
				if (index < 0) {
					continue;
				}

				if (index < bestIndex || index == bestIndex && name.length() > bestLength) {
					best = info;
					bestIndex = index;
					bestLength = name.length();
				}
			}
		}

		return best;
	}

	private static String[] namesOf(PlayerInfo info) {
		String profile = plain(info.getProfile().name());
		Component display = info.getTabListDisplayName();
		if (display == null) {
			return usable(profile) ? new String[] {profile} : new String[0];
		}

		String displayName = plain(display.getString());
		if (!usable(displayName) || displayName.equals(profile)) {
			return usable(profile) ? new String[] {profile} : new String[0];
		}

		if (!usable(profile)) {
			return new String[] {displayName};
		}

		return new String[] {profile, displayName};
	}

	private static boolean usable(String name) {
		return name.length() >= MIN_NAME_LENGTH && !name.startsWith("|");
	}

	private static String plain(String name) {
		return FORMATTING.matcher(name).replaceAll("");
	}

	private static int indexOfName(String text, String name) {
		int from = 0;
		while (from <= text.length() - name.length()) {
			int index = text.indexOf(name, from);
			if (index < 0) {
				return -1;
			}

			if (isSeparated(text, index, name.length())) {
				return index;
			}

			from = index + 1;
		}

		return -1;
	}

	private static boolean isSeparated(String text, int index, int length) {
		if (index > 0 && isNameCharacter(text.charAt(index - 1))) {
			return false;
		}

		int end = index + length;
		return end >= text.length() || !isNameCharacter(text.charAt(end));
	}

	private static boolean isNameCharacter(char character) {
		return Character.isLetterOrDigit(character) || character == '_';
	}
}
