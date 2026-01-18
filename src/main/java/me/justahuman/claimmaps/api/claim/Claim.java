package me.justahuman.claimmaps.api.claim;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.justahuman.claimmaps.implementation.claim.ClaimManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Claim(long id, UUID owner, String customName, RegistryKey<World> worldKey, List<ChunkPos> chunks, int color) {
    private static final int DATA_VERSION = 1;

    public String display() {
        StringBuilder name = new StringBuilder("□ ");
        if (!customName.isEmpty()) {
            name.append(customName).append(" - ");
        }
        name.append(ClaimManager.getOwnerName(owner)).append("'s Claim");
        return name.toString();
    }

    public Text displayText() {
        Text text = Text.literal("□ ").styled((s -> s.withColor(color)));
        text.getSiblings().add(Text.literal(ClaimManager.getOwnerName(owner) + "'s Claim").formatted(Formatting.WHITE));
        if (!customName.isEmpty()) {
            text.getSiblings().add(0, Text.literal(I18n.translate(customName) + " - ").formatted(Formatting.WHITE));
        }
        return text;
    }

    public byte[] serialize() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeInt(DATA_VERSION);
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
        int version = input.readInt();
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
