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
import uk.bobbytables.topoislands.world.TopoIslandsSaveData;

public class IslandInviteAccept implements Command<CommandSource> {
    private static final IslandInviteAccept INSTANCE = new IslandInviteAccept();

    private static final SimpleCommandExceptionType NO_INVITE = new SimpleCommandExceptionType(new StringTextComponent("No pending invites"));

    public static ArgumentBuilder<CommandSource, ?> register (CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("accept").executes(IslandInviteAccept.INSTANCE);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();

        Integer islandId = IslandInvite.PENDING_INVITES.remove(player.getUUID());
        if (islandId == null) {
            throw NO_INVITE.create();
        }

        TopoIslandsSaveData saveData = player.getLevel().getDataStorage().computeIfAbsent(TopoIslandsSaveData::new, TopoIslandsSaveData.NAME);
        saveData.assignPlayerToIsland(player, islandId);
        Utils.teleportToIsland(player, islandId);

        return 0;
    }
}
