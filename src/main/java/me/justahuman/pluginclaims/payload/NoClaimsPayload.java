package me.justahuman.pluginclaims.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public record NoClaimsPayload(RegistryKey<World> worldKey, ChunkPos chunkPos) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, NoClaimsPayload> CODEC =
            Payloads.newCodec(input -> new NoClaimsPayload(
                    RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(input.readUTF())),
                    new ChunkPos(input.readInt(), input.readInt())));

    @Override
    public Id<? extends CustomPayload> getId() {
        return Payloads.NO_CLAIMS;
    }
}