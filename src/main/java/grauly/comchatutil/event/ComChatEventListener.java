package grauly.comchatutil.event;

import grauly.comchatutil.ComChatUtil;
import grauly.comchatutil.config.ComChatConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ComChatEventListener {
    private ComChatEventListener() {
        throw new IllegalStateException("Utility class");
    }

    private static ClientPacketListener networkHandler;
    public static boolean needsRegexRecompile = true;
    private static String fullEscapingRegex;

    public static String applyAliases(String command) {
        var config = AutoConfig.getConfigHolder(ComChatConfig.class).getConfig();
        // toggle aliases
        if (config.aliases.contains(command)) {
            return config.getTogglePhrase();
        }
        // inline aliases
        for (String alias : config.aliases) {
            if (command.startsWith(alias)) {
                return command.replaceFirst(alias, config.getInlinePhrase());
            }
        }
        return command;
    }

    public static void handleComChatToggle(String command) {
        var config = AutoConfig.getConfigHolder(ComChatConfig.class).getConfig();
        var lowerCase = command.toLowerCase();
        if (config.togglePhrases.contains(lowerCase)) {
            ComChatUtil.inComChat.set(!ComChatUtil.inComChat.get());
        }
    }

    public static boolean handleComChatEscaping(String message) {
        var config = AutoConfig.getConfigHolder(ComChatConfig.class).getConfig();
        if (needsRegexRecompile) {
            createRegexFromUserConfig();
            needsRegexRecompile = false;
        }
        if (ComChatUtil.inComChat.get() && message.matches(fullEscapingRegex)) {
            networkHandler.sendCommand(config.getInlinePhrase());
            try {
                Thread.sleep(50L * config.escapeDelayTicks);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            networkHandler.sendChat(message);
            try {
                Thread.sleep(50L * config.escapeDelayTicks);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            networkHandler.sendCommand(config.getInlinePhrase());
            return false;
        }
        return true;
    }

    public static void handleJoin(ClientPacketListener clientPacketListener, PacketSender sender, Minecraft minecraft) {
        ComChatUtil.inComChat.set(false);
        networkHandler = clientPacketListener;
    }

    public static void createRegexFromUserConfig() {
        StringBuilder builder = new StringBuilder();
        ArrayList<String> errorStrings = new ArrayList<>();
        ComChatConfig config = AutoConfig.getConfigHolder(ComChatConfig.class).getConfig();
        config.escapedPhrases.forEach(phrase -> {
            try {
                Pattern.compile(phrase);
                builder.append(phrase);
                builder.append("|");
            } catch (PatternSyntaxException e) {
                errorStrings.add(phrase);
            }
        });
        builder.deleteCharAt(builder.length() - 1);
        fullEscapingRegex = builder.toString();
        createErrorMessage(errorStrings);
    }

    public static void createErrorMessage(List<String> errorStrings) {
        if (errorStrings.isEmpty()) {
            return;
        }
        MutableComponent errorText = Component.translatable("text.comchatutil.regex.error");
        errorStrings.forEach(erroringString -> {
            errorText.append(Component.literal("\n"));
            errorText.append(Component.literal(erroringString));
        });
        ComChatUtil.LOGGER.warn((errorText.getString()));
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(errorText);
        }
    }

    public static InteractionResult onConfigChange(ConfigHolder<ComChatConfig> comChatConfigConfigHolder, ComChatConfig comChatConfig) {
        ComChatUtil.LOGGER.info("Config save detected, recompiling regex");
        createRegexFromUserConfig();
        needsRegexRecompile = false;
        return InteractionResult.PASS;
    }
}
