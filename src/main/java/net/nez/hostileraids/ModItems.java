package net.nez.hostileraids;

// Minecraft and Forge item registration imports
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.Items;

public class ModItems {
    // DeferredRegister for items, using the mod's ID
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HostileRaids.MODID);

    // Method to register items with the event bus
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    // Optional utility method to check if an item triggers a raid
    public static boolean isRaidTrigger(Item item) {
        return item == Items.ROTTEN_FLESH;
    }
}