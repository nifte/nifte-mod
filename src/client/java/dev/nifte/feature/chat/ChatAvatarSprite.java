package dev.nifte.feature.chat;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.world.item.component.ResolvableProfile;

final class ChatAvatarSprite {
	private ChatAvatarSprite() {
	}

	static Component prepend(Component message, ResolvableProfile profile, boolean hat) {
		return Component.empty()
			.append(Component.object(new PlayerSprite(profile, hat)))
			.append(" ")
			.append(message);
	}

	static boolean hasPlayerSprite(Component component) {
		if (component.getContents() instanceof ObjectContents contents) {
			if (contents.contents() instanceof PlayerSprite) {
				return true;
			}

			Optional<Component> fallback = contents.fallback();
			if (fallback.isPresent() && hasPlayerSprite(fallback.get())) {
				return true;
			}
		}

		if (component.getContents() instanceof TranslatableContents translatable) {
			for (Object arg : translatable.getArgs()) {
				if (arg instanceof Component nested && hasPlayerSprite(nested)) {
					return true;
				}
			}
		}

		for (Component sibling : component.getSiblings()) {
			if (hasPlayerSprite(sibling)) {
				return true;
			}
		}

		return false;
	}
}
