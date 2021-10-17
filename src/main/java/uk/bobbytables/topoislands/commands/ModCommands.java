package uk.bobbytables.topoislands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

public class ModCommands {
    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> commands = dispatcher.register(
                Commands.literal("topoislands")
                        .then(IslandNew.register(dispatcher))
                        .then(IslandAssign.register(dispatcher))
                        .then(IslandInvite.register(dispatcher))
                        .then(IslandInviteAccept.register(dispatcher))
                        .then(IslandInfo.register(dispatcher))
                        .then(IslandHome.register(dispatcher))
                        .then(IslandSpawn.register(dispatcher))
        );
    }
}
