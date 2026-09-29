package one.ggsky.alternativeauth.mixin;

import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ElyByChatSessionMixin {

    @Shadow
    private net.minecraft.server.level.ServerPlayer player;

    @Inject(
            method = "handleChatSessionUpdate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void alternativeAuth$skipElyByChatSession(
            ServerboundChatSessionUpdatePacket packet,
            CallbackInfo ci
    ) {
        if (player == null || player.getGameProfile() == null) {
            return;
        }

        PropertyMap properties = player.getGameProfile().properties();

        if (!properties.get("ely").isEmpty()) {
            ci.cancel();
        }
    }
}