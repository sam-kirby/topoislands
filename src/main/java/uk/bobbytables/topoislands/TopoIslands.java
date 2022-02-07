package uk.bobbytables.topoislands;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.bobbytables.topoislands.commands.ModCommands;

import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("topoislands")
public class TopoIslands {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final UUID CHAT_UUID = UUID.fromString("25f3467a-d789-40c6-8a76-b7331009ac37");

    public TopoIslands() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY,
                (v, n) -> true
        ));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TopoIslandsConfig.serverSpec);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.registerCommands(event.getDispatcher());
    }
}
