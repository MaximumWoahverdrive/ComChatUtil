package grauly.comchatutil.bridge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Sent by servers that know which chat the player is in, so the mod doesn't have to guess from typed commands.
 * <p>
 * Wire format of {@code comchatutil:chat_state}: three bytes, version (1), the chats the server reports, the chats
 * that are on. Bit 0 is community chat, bit 1 staff chat. Chats the server doesn't report are left to the mod's own
 * guessing. Bytes after these are skipped, so servers can append fields without a new version.
 */
public record ChatStatePayload(int reported, int active) implements CustomPacketPayload {
    public static final int COMMUNITY = 1;
    public static final int STAFF = 1 << 1;
    private static final int VERSION = 1;

    public static final Type<ChatStatePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("comchatutil", "chat_state"));
    public static final StreamCodec<FriendlyByteBuf, ChatStatePayload> CODEC =
            StreamCodec.of(ChatStatePayload::write, ChatStatePayload::read);

    public boolean reports(int chat) {
        return (reported & chat) != 0;
    }

    public boolean isOn(int chat) {
        return (active & chat) != 0;
    }

    private static ChatStatePayload read(FriendlyByteBuf buf) {
        int version = buf.readUnsignedByte();
        // an unknown version reports nothing, so the mod keeps guessing instead of trusting bytes it can't read
        ChatStatePayload payload = version == VERSION
                ? new ChatStatePayload(buf.readUnsignedByte(), buf.readUnsignedByte())
                : new ChatStatePayload(0, 0);
        buf.skipBytes(buf.readableBytes());
        return payload;
    }

    private static void write(FriendlyByteBuf buf, ChatStatePayload payload) {
        buf.writeByte(VERSION);
        buf.writeByte(payload.reported);
        buf.writeByte(payload.active);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
