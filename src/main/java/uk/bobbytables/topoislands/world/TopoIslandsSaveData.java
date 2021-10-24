package uk.bobbytables.topoislands.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TopoIslandsSaveData extends WorldSavedData {
    public static final String NAME = "topoislands_save_data";
    private static final int ISLAND_SPACING = 1000;
    private final List<Set<UUID>> index = new ArrayList<>();

    public TopoIslandsSaveData() {
        super(TopoIslandsSaveData.NAME);
        this.index.add(new HashSet<>());
    }

    public static BlockPos convertIndexToCoords(int index) {
        int dx = 1;
        int dz = 0;
        int sl = 1;

        int x = 0;
        int z = 0;
        int s = 0;

        for (int i = 0; i < index; i++) {
            x += dx;
            z += dz;

            if (++s == sl) {
                s = 0;
                int dx_t = dx;
                dx = -dz;
                dz = dx_t;

                if (dz == 0) {
                    ++sl;
                }
            }
        }

        return new BlockPos(x * ISLAND_SPACING, 0, z * ISLAND_SPACING);
    }

    @Override
    public void load(CompoundNBT p_76184_1_) {
        this.index.clear();
        if (p_76184_1_.contains("Index")) {
            ListNBT indexNbt = p_76184_1_.getList("Index", 9);
            for (int i = 0; i < indexNbt.size(); i++) {
                ListNBT playersNbt = indexNbt.getList(i);
                Set<UUID> islandPlayers = new HashSet<>();
                for (int p = 0; p < playersNbt.size(); p += 2) {
                    LongNBT mostSig = (LongNBT) playersNbt.get(p);
                    LongNBT leastSig = (LongNBT) playersNbt.get(p + 1);

                    islandPlayers.add(new UUID(mostSig.getAsLong(), leastSig.getAsLong()));
                }

                this.index.add(islandPlayers);
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT p_189551_1_) {
        ListNBT indexNbt = new ListNBT();
        for (Set<UUID> islandPlayers : this.index) {
            ListNBT playersNbt = new ListNBT();
            for (UUID player : islandPlayers) {
                playersNbt.add(LongNBT.valueOf(player.getMostSignificantBits()));
                playersNbt.add(LongNBT.valueOf(player.getLeastSignificantBits()));
            }
            indexNbt.add(playersNbt);
        }

        p_189551_1_.put("Index", indexNbt);

        return p_189551_1_;
    }

    public int createIslandWithOwner(ServerPlayerEntity owner) {
        clearIslandAssignment(owner);

        this.index.add(new HashSet<>(Collections.singleton(owner.getUUID())));
        this.setDirty();
        return this.index.size() - 1;
    }

    public List<String> getPlayersOnIsland(int islandId, MinecraftServer server) {
        return this.index.get(islandId).stream().map(uuid -> server.getProfileCache().get(uuid)).filter(Objects::nonNull).map(GameProfile::getName).collect(Collectors.toList());
    }

    public int getIslandForPlayer(ServerPlayerEntity query) {
        return IntStream.range(0, this.index.size()).filter(i -> this.index.get(i).contains(query.getUUID())).findFirst().orElse(0);
    }

    public int getNextIslandId() {
        return this.index.size();
    }

    public void assignPlayerToIsland(ServerPlayerEntity player, int islandId) {
        clearIslandAssignment(player);

        if (islandId < this.index.size()) {
            this.index.get(islandId).add(player.getUUID());
            this.setDirty();
        }
    }

    public void clearIslandAssignment(ServerPlayerEntity player) {
        int existingIsland = getIslandForPlayer(player);
        if (existingIsland != 0) {
            this.index.get(existingIsland).remove(player.getUUID());
        }
        this.setDirty();
    }
}
