package grauly.comchatutils.mixin;

import grauly.comchatutils.ComChatUtil;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(at = @At("HEAD"), method = "sendChatCommand")
    public void sendChatCommand(String command, CallbackInfo ci) {
        if(command.equals("cc") || command.equals("communitychat")) {
            ComChatUtil.inComChat = !ComChatUtil.inComChat;
        }
    }

    @Inject(at = @At("HEAD"), method = "onGameJoin")
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        ComChatUtil.inComChat = false;
    }
}