package me.justahuman.claimmaps.payload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.justahuman.claimmaps.util.ClaimMapUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.function.Function;

public class Payloads {
    public static final CustomPayload.Id<ClaimPayload> CLAIM = newChannel("claim");
    public static final CustomPayload.Id<NoClaimsPayload> NO_CLAIMS = newChannel("no_claims");
    public static final CustomPayload.Id<DeleteClaimPayload> DELETE_CLAIM = newChannel("delete_claim");

    public static <P extends CustomPayload> CustomPayload.Id<P> newChannel(String channel) {
        return new CustomPayload.Id<>(ClaimMapUtils.id(channel));
    }

    public static <P extends CustomPayload> PacketCodec<PacketByteBuf, P> newCodec(Function<ByteArrayDataInput, P> decoder) {
        return PacketCodec.of((value, buf) -> {}, buf -> {
            byte[] bytes = new byte[buf.readableBytes()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = buf.readByte();
            }
            return decoder.apply(ByteStreams.newDataInput(bytes));
        });
    }
}
