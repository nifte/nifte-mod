package dev.nifte.mixin;

import java.util.List;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {
	@Accessor("allMessages")
	List<GuiMessage> nifte$allMessages();

	@Accessor("chatScrollbarPos")
	int nifte$getChatScrollbarPos();

	@Accessor("chatScrollbarPos")
	void nifte$setChatScrollbarPos(int position);

	@Accessor("newMessageSinceScroll")
	boolean nifte$isNewMessageSinceScroll();

	@Accessor("newMessageSinceScroll")
	void nifte$setNewMessageSinceScroll(boolean pending);

	@Invoker("refreshTrimmedMessages")
	void nifte$refreshTrimmedMessages();
}
