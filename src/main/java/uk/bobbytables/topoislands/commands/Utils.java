package uk.bobbytables.topoislands.commands;

import com.bloodnbonesgaming.topography.common.config.ConfigurationManager;
import com.bloodnbonesgaming.topography.common.config.DimensionDef;
import com.bloodnbonesgaming.topography.common.config.Preset;
import com.bloodnbonesgaming.topography.common.util.StructureHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.bobbytables.topoislands.world.TopoIslandsSaveData;

public class Utils {
    public static void teleportToIsland(ServerPlayerEntity player, int islandId) {
        BlockPos islandPos = TopoIslandsSaveData.convertIndexToCoords(islandId);

        Preset topoPreset = ConfigurationManager.getGlobalConfig().getPreset();
        DimensionDef def = topoPreset.defs.get(World.OVERWORLD.location());
        BlockPos islandCoords = TopoIslandsSaveData.convertIndexToCoords(islandId).above(def.spawnStructureHeight);

        BlockPos spawn_offset = StructureHelper.getSpawn(def.spawnStructure);

        if (spawn_offset != null) {
            BlockPos spawn = islandCoords.offset(spawn_offset);
            player.getLevel().setBlock(spawn, Blocks.AIR.defaultBlockState(), 2);
            player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        }
    }
}
