package grauly.comchatutil.mixin;

import grauly.comchatutil.ComChatUtil;
import grauly.comchatutil.config.ComChatConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected EditBox input;

    @Inject(at = @At("TAIL"), method = "onEdited")
    private void onEdited(String chatText, CallbackInfo ci) {
        if (ComChatUtil.inComChat.get()) {
            ComChatConfig config = AutoConfig.getConfigHolder(ComChatConfig.class).getConfig();
            // text colors are ARGB since 1.21.x; the config stores RGB only
            input.setTextColor(ARGB.opaque(config.color));
        } else {
            input.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
        }
    }
}
