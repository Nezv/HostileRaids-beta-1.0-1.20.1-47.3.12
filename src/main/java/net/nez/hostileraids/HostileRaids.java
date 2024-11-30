package net.nez.hostileraids;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HostileRaids.MODID)
public class HostileRaids {
    public static final String MODID = "hostileraids";
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
    
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        // Logs a message when the mod is setting up
        LOGGER.info("Hostile Raids mod is setting up");
    }
}