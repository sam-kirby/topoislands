package uk.bobbytables.topoislands;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TopoIslandsConfig {
    public static class Server {
        public final ForgeConfigSpec.IntValue islandSeparation;
        public final ForgeConfigSpec.BooleanValue spawnUsesWorldSpawn;

        Server(ForgeConfigSpec.Builder builder) {
            islandSeparation = builder.comment("The grid spacing to use between islands").defineInRange("islandSeparation", 1000, 100, 10000);
            spawnUsesWorldSpawn = builder.comment("Should running /topoislands spawn take the user to the world spawn?").define("spawnUsesWorldSpawn", false);
        }
    }

    static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;
    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }
}
