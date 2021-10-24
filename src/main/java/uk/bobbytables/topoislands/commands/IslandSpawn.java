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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import uk.bobbytables.topoislands.world.TopoIslandsSaveData;

public class IslandSpawn implements Command<CommandSource> {
    private static final IslandSpawn INSTANCE = new IslandSpawn();

    private static final SimpleCommandExceptionType OWNER_NOT_IN_OVERWORLD = new SimpleCommandExceptionType(new StringTextComponent("Target is not in the overworld"));

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("spawn").executes(IslandSpawn.INSTANCE);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity target = context.getSource().getPlayerOrException();

        if (target.getLevel().dimension() != World.OVERWORLD) {
            throw OWNER_NOT_IN_OVERWORLD.create();
        }

        Utils.teleportToIsland(target, 0);

        return 0;
    }
}
