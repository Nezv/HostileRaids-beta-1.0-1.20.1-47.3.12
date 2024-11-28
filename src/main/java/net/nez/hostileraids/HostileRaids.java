package net.nez.hostileraids; // Defines the package for this mod

// Importing necessary Minecraft and Forge libraries
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HostileRaids.MODID) // Tells Forge this is a mod, using the MODID constant
public class HostileRaids {
    // Mod's unique identifier
    public static final String MODID = "hostileraids";

    // Logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public HostileRaids() {
        // Get the event bus for mod-specific events
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register different mod components
        ModEntityTypes.register(modEventBus);  // Register custom entity types
        ModItems.register(modEventBus);        // Register custom items
        ModEvents.register();                  // Register event handlers

        // Optional method for initial mod setup
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        // Logs a message when the mod is setting up
        LOGGER.info("Hostile Raids mod is setting up");
    }
}