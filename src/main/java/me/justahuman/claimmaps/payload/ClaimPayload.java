package me.justahuman.claimmaps.payload;

import me.justahuman.claimmaps.claim.Claim;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ClaimPayload(Claim claim) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, ClaimPayload> CODEC =
            Payloads.newCodec(input -> new ClaimPayload(Claim.deserialize(input)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return Payloads.CLAIM;
    }
}
