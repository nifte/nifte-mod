package dev.nifte.mixin;

import java.time.Instant;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.chat.ChatAvatars;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
	@Inject(method = "showMessageToPlayer", at = @At("HEAD"))
	private void nifte$captureChatSender(
		ChatType.Bound boundChatType,
		PlayerChatMessage message,
		Component decoratedMessage,
		GameProfile sender,
		boolean onlyShowSecure,
		Instant received,
		CallbackInfoReturnable<Boolean> cir
	) {
		ChatAvatars.captureSender(message.sender(), sender);
	}

	@Inject(method = "showMessageToPlayer", at = @At("RETURN"))
	private void nifte$clearChatSender(CallbackInfoReturnable<Boolean> cir) {
		ChatAvatars.clearSender();
	}
}
