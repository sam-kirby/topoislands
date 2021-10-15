package uk.bobbytables.topoislands.commands;

import com.bloodnbonesgaming.topography.common.config.ConfigurationManager;
import com.bloodnbonesgaming.topography.common.config.DimensionDef;
import com.bloodnbonesgaming.topography.common.config.Preset;
import com.bloodnbonesgaming.topography.common.util.StructureHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.server.ServerWorld;
import uk.bobbytables.topoislands.TopoIslands;
import uk.bobbytables.topoislands.world.TopoIslandsSaveData;

public class IslandNew implements Command<CommandSource> {
    private static final IslandNew INSTANCE = new IslandNew();

    private static final SimpleCommandExceptionType OWNER_NOT_IN_OVERWORLD = new SimpleCommandExceptionType(new StringTextComponent("Provided owner is not in the overworld"));
    private static final SimpleCommandExceptionType NOT_TOPOGRAPHY_WORLD = new SimpleCommandExceptionType(new StringTextComponent("The current world is not using Topography world generation"));
    private static final SimpleCommandExceptionType NO_SPAWN_STRUCTURE = new SimpleCommandExceptionType(new StringTextComponent("No spawn structure is configured in the overworld"));

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("new").requires(cs -> cs.hasPermission(4))
                .then(Commands.argument("owner", EntityArgument.player()).executes(IslandNew.INSTANCE));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity owner = EntityArgument.getPlayer(context, "owner");
        ServerWorld tWorld = owner.getLevel();
        if (owner.level.dimension() != World.OVERWORLD) {
            throw OWNER_NOT_IN_OVERWORLD.create();
        }

        Preset topoPreset = ConfigurationManager.getGlobalConfig().getPreset();
        if (topoPreset == null) {
            throw NOT_TOPOGRAPHY_WORLD.create();
        }
        DimensionDef def = topoPreset.defs.get(World.OVERWORLD.location());
        if (def == null || def.spawnStructure == null) {
            throw NO_SPAWN_STRUCTURE.create();
        }

        TopoIslands.LOGGER.info("Loading save data");
        TopoIslandsSaveData saveData = (TopoIslandsSaveData) tWorld.getDataStorage().computeIfAbsent(TopoIslandsSaveData::new, TopoIslandsSaveData.NAME);
        int islandIndex = saveData.createIslandWithOwner(owner);
        BlockPos islandCoords = TopoIslandsSaveData.convertIndexToCoords(islandIndex).above(def.spawnStructureHeight);
        TopoIslands.LOGGER.info("Trying to create island with index {} at {}", islandIndex, islandCoords);
        TopoIslands.LOGGER.info("Requested by {} on behalf of {}", context.getSource().getDisplayName().getString(), owner.getDisplayName().getString());

        // Preload
        TopoIslands.LOGGER.info("Beginning preloading");
        BlockPos size = def.spawnStructure.getSize();
        int length = Math.max(size.getX(), size.getZ());
        length /= 16; // To chunk length
        length = Math.max(length, 4);

        int xc = islandCoords.getX() >> 4;
        int zc = islandCoords.getZ() >> 4;

        for (int x = xc - length; x <= xc + length; x++) {
            for (int z = zc - length; z <= zc + length; z++) {
                tWorld.getChunkSource().getChunk(x, z, true);
            }
        }

        TopoIslands.LOGGER.info("Placing structure");
        def.spawnStructure.placeInWorld(tWorld, islandCoords, new PlacementSettings(), tWorld.random);

        BlockPos spawn_offset = StructureHelper.getSpawn(def.spawnStructure);

        if (spawn_offset != null) {
            TopoIslands.LOGGER.info("Preparing spawn");
            BlockPos spawn = islandCoords.offset(spawn_offset);
            tWorld.setBlock(spawn, Blocks.AIR.defaultBlockState(), 2);

            TopoIslands.LOGGER.info("Teleporting owner to new island :)");
            owner.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        }

        context.getSource().sendSuccess(new StringTextComponent("Island " + islandIndex + " created successfully at " + islandCoords.getX() + ", " + islandCoords.getZ()), true);

        return 0;
    }
}
