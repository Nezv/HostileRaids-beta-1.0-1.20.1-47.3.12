package net.nez.hostileraids;

// Minecraft and Forge entity registration imports
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntityTypes {
    // Creates a DeferredRegister for entity types
    // This is a Forge mechanism for registering game objects
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HostileRaids.MODID);

    // Method to register entity types with the event bus
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    // You can add custom entity registrations here if needed
    // Example:
    // public static final RegistryObject<EntityType<CustomZombie>> CUSTOM_ZOMBIE =
    //     ENTITY_TYPES.register("custom_zombie",
    //         () -> EntityType.Builder.of(CustomZombie::new, MobCategory.MONSTER)
    //             .sized(0.6f, 1.95f)
    //             .build(new ResourceLocation(HostileRaids.MODID, "custom_zombie").toString()));
}