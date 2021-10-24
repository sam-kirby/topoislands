package uk.bobbytables.topoislands.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import uk.bobbytables.topoislands.TopoIslands;
import uk.bobbytables.topoislands.world.TopoIslandsSaveData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandInvite implements Command<CommandSource> {
    private static final IslandInvite INSTANCE = new IslandInvite();

    private static final SimpleCommandExceptionType INVITER_NO_ISLAND = new SimpleCommandExceptionType(new StringTextComponent("You must be assigned to an island to use this command"));

    public static final Map<UUID, Integer> PENDING_INVITES = new HashMap<>();

    public static ArgumentBuilder<CommandSource, ?> register (CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("invite")
                .then(Commands.argument("invitee", EntityArgument.player()).executes(IslandInvite.INSTANCE));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity inviter = context.getSource().getPlayerOrException();
        ServerPlayerEntity invitee = EntityArgument.getPlayer(context, "invitee");

        TopoIslandsSaveData saveData = inviter.getLevel().getDataStorage().computeIfAbsent(TopoIslandsSaveData::new, TopoIslandsSaveData.NAME);

        int inviter_island = saveData.getIslandForPlayer(inviter);

        if (inviter_island == 0) {
            throw INVITER_NO_ISLAND.create();
        }

        PENDING_INVITES.put(invitee.getUUID(), inviter_island);

        invitee.sendMessage(inviter.getDisplayName().copy().append(" is inviting you to their island"), TopoIslands.CHAT_UUID);
        invitee.sendMessage(new StringTextComponent("Click here to accept").withStyle(Style.EMPTY.withColor(TextFormatting.DARK_GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/topoislands accept"))), TopoIslands.CHAT_UUID);

        return 0;
    }
}
