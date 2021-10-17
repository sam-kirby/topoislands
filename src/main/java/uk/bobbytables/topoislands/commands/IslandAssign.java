package uk.bobbytables.topoislands.commands;

import com.bloodnbonesgaming.topography.common.config.ConfigurationManager;
import com.bloodnbonesgaming.topography.common.config.DimensionDef;
import com.bloodnbonesgaming.topography.common.config.Preset;
import com.bloodnbonesgaming.topography.common.util.StructureHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.world.server.ServerWorld;
import uk.bobbytables.topoislands.world.TopoIslandsSaveData;

public class IslandAssign implements Command<CommandSource> {
    private static final IslandAssign INSTANCE = new IslandAssign();

    private static final SimpleCommandExceptionType OWNER_NOT_IN_OVERWORLD = new SimpleCommandExceptionType(new StringTextComponent("Target is not in the overworld"));
    private static final SimpleCommandExceptionType ISLAND_NONEXISTANT = new SimpleCommandExceptionType(new StringTextComponent("Island does not exist - create a new island with the /topoislands new command"));

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("assign").requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("assignee", EntityArgument.player())
                        .then(Commands.argument("islandId", IntegerArgumentType.integer())
                                .executes(IslandAssign.INSTANCE)));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(context, "assignee");
        ServerWorld tWorld = target.getLevel();

        if (tWorld.dimension() != World.OVERWORLD) {
            throw OWNER_NOT_IN_OVERWORLD.create();
        }

        int islandId = IntegerArgumentType.getInteger(context, "islandId");

        TopoIslandsSaveData saveData = tWorld.getDataStorage().computeIfAbsent(TopoIslandsSaveData::new, TopoIslandsSaveData.NAME);

        if (saveData.getNextIslandId() <= islandId) {
            throw ISLAND_NONEXISTANT.create();
        }

        saveData.assignPlayerToIsland(target, islandId);

        Preset topoPreset = ConfigurationManager.getGlobalConfig().getPreset();
        DimensionDef def = topoPreset.defs.get(World.OVERWORLD.location());
        BlockPos islandCoords = TopoIslandsSaveData.convertIndexToCoords(islandId).above(def.spawnStructureHeight);

        BlockPos spawn_offset = StructureHelper.getSpawn(def.spawnStructure);

        if (spawn_offset != null) {
            BlockPos spawn = islandCoords.offset(spawn_offset);
            tWorld.setBlock(spawn, Blocks.AIR.defaultBlockState(), 2);
            target.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        }

        return 0;
    }
}
