package uk.bobbytables.topoislands.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import uk.bobbytables.topoislands.world.TopoIslandsSaveData;

public class IslandInfo implements Command<CommandSource> {
    private static final IslandInfo INSTANCE = new IslandInfo();

    private static final SimpleCommandExceptionType OWNER_NOT_IN_OVERWORLD = new SimpleCommandExceptionType(new StringTextComponent("Target is not in the overworld"));

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("info").executes(IslandInfo.INSTANCE);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        if (player.getLevel().dimension() != World.OVERWORLD) {
            throw OWNER_NOT_IN_OVERWORLD.create();
        }

        TopoIslandsSaveData saveData = player.getLevel().getDataStorage().computeIfAbsent(TopoIslandsSaveData::new, TopoIslandsSaveData.NAME);

        int islandId = saveData.getIslandForPlayer(player);
        BlockPos islandPos = TopoIslandsSaveData.convertIndexToCoords(islandId);

        context.getSource().sendSuccess(new StringTextComponent("Your island is " + islandId + " at " + islandPos.getX() + ", " + islandPos.getZ()), false);

        return 0;
    }
}
