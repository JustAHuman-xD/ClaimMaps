package me.justahuman.xaeropluginclaims.claim;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Claim(long id, UUID owner, String customName, RegistryKey<World> worldKey, List<ChunkPos> chunks, int color) {
    public byte[] serialize() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeLong(id);
        output.writeLong(owner == null ? -1 : owner.getMostSignificantBits());
        output.writeLong(owner == null ? -1 : owner.getLeastSignificantBits());
        output.writeUTF(customName);
        output.writeUTF(worldKey.getValue().toString());
        output.writeInt(chunks.size());
        for (ChunkPos chunk : chunks) {
            output.writeInt(chunk.x);
            output.writeInt(chunk.z);
        }
        output.writeInt(color);
        return output.toByteArray();
    }

    public static Claim deserialize(byte[] data) {
        return deserialize(ByteStreams.newDataInput(data));
    }

    public static Claim deserialize(ByteArrayDataInput input) {
        long id = input.readLong();
        long mostSigBits = input.readLong();
        long leastSigBits = input.readLong();
        UUID owner = mostSigBits == -1 && leastSigBits == -1 ? null : new UUID(mostSigBits, leastSigBits);
        String customName = input.readUTF();
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(input.readUTF()));
        int chunkCount = input.readInt();
        List<ChunkPos> chunks = new ArrayList<>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            int chunkX = input.readInt();
            int chunkZ = input.readInt();
            chunks.add(new ChunkPos(chunkX, chunkZ));
        }
        int color = input.readInt();
        return new Claim(id, owner, customName, worldKey, chunks, color);
    }
}
