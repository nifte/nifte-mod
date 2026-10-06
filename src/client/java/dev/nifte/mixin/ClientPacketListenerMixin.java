package dev.nifte.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.chat.ChatAvatars;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "handlePlayerInfoUpdate", at = @At("RETURN"))
	private void nifte$avatarForJoinedPlayers(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
		ClientPacketListener listener = (ClientPacketListener) (Object) this;
		for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.newEntries()) {
			PlayerInfo player = listener.getPlayerInfo(entry.profileId());
			if (player != null) {
				ChatAvatars.attachListedPlayer(player);
			}
		}
	}
}
