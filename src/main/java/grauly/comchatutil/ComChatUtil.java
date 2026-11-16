package grauly.comchatutil;

import grauly.comchatutil.bridge.ChatStatePayload;
import grauly.comchatutil.config.ComChatConfig;
import grauly.comchatutil.config.ComChatDefaultConfig;
import grauly.comchatutil.event.ComChatEventListener;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComChatUtil implements ClientModInitializer {

    public static final AtomicBoolean inComChat = new AtomicBoolean(false);
    public static final AtomicBoolean inStaffChat = new AtomicBoolean(false);
    /** Whether the server sends the community chat state, in which case typed commands are not counted. */
    public static volatile boolean serverReportsComChat = false;
    public static final Logger LOGGER = LoggerFactory.getLogger("comchatutil");

    @Override
    public void onInitializeClient() {
        // checked before registering, which writes a default config if there is none
        boolean hadConfig = Files.exists(FabricLoader.getInstance().getConfigDir().resolve("comchatutil.json"));
        AutoConfig.register(ComChatConfig.class, GsonConfigSerializer::new);
        var configHolder = AutoConfig.getConfigHolder(ComChatConfig.class);
        configHolder.registerSaveListener((_, savedConfig) -> ComChatEventListener.onConfigChange(savedConfig));
        var config = configHolder.getConfig();
        if (config.configVersion < ComChatDefaultConfig.CONFIG_VERSION) {
            if (hadConfig && config.hasLegacyDefaults()) {
                // the version is raised once the notice was shown, so quitting before joining doesn't lose it
                ComChatEventListener.showUpgradeNotice = true;
            } else {
                config.configVersion = ComChatDefaultConfig.CONFIG_VERSION;
                configHolder.save();
            }
        }
        ClientSendMessageEvents.MODIFY_COMMAND.register(ComChatEventListener::applyAliases);
        ClientSendMessageEvents.ALLOW_CHAT.register(ComChatEventListener::handleComChatEscaping);
        ClientSendMessageEvents.COMMAND.register(ComChatEventListener::handleComChatToggle);
        ClientPlayConnectionEvents.JOIN.register((clientPacketListener, sender, minecraft) -> ComChatEventListener.handleJoin(clientPacketListener, minecraft));
        // registering the receiver also tells the server we listen on the channel
        PayloadTypeRegistry.clientboundPlay().register(ChatStatePayload.TYPE, ChatStatePayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ChatStatePayload.TYPE,
                (payload, context) -> ComChatEventListener.handleChatState(payload));
    }
}
