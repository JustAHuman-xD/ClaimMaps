package me.justahuman.xaeropluginclaims.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public record DeleteClaimPayload(RegistryKey<World> worldKey, long id) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, DeleteClaimPayload> CODEC =
            Payloads.newCodec(input -> new DeleteClaimPayload(RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(input.readUTF())), input.readLong()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return Payloads.DELETE_CLAIM;
    }
}
