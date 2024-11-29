package net.nez.hostileraids;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;

import java.util.Random;

@Mod.EventBusSubscriber(modid = HostileRaids.MODID)
public class ModEvents {
    private static final Random RANDOM = new Random();

    public static void register() {
        // Register the events to the Forge event bus
        MinecraftForge.EVENT_BUS.register(ModEvents.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only trigger on server side and when player has rotten flesh
        if (!event.player.level().isClientSide() &&
                event.player.getInventory().contains(Items.ROTTEN_FLESH.getDefaultInstance())) {

            triggerZombieRaid(event.player.level(), event.player.blockPosition());
        }
    }

    private static void triggerZombieRaid(Level world, BlockPos center) {
        // Ensure we're in a server world and can support raids
        if (!(world instanceof ServerLevel serverLevel) ||
                world.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        // Create a custom raid with a default bad omen level
        Raid zombieRaid = Raid.create(serverLevel, 1, center);

        // Customize raid waves and zombie types
        for (int wave = 1; wave <= 5; wave++) {
            spawnZombieWave(serverLevel, zombieRaid, center, wave);
        }

        // Play raid start sound
        world.playSound(null, center, SoundEvents.RAID_HORN, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void spawnZombieWave(ServerLevel world, Raid raid, BlockPos center, int wave) {
        // Determine zombie types based on wave and difficulty
        int zombieCount = calculateZombieCount(world, wave);

        for (int i = 0; i < zombieCount; i++) {
            // Randomize zombie type
            Zombie zombie = createSpecialZombie(world, wave);

            // Position with some randomness
            BlockPos spawnPos = getRandomNearbyPos(center);
            zombie.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

            // Add to world
            world.addFreshEntity(zombie);
        }
    }

    private static Zombie createSpecialZombie(ServerLevel world, int wave) {
        // Create different zombie types based on wave
        switch (wave) {
            case 1:
                return new Zombie(world);
            case 2:
                // Chance for zombie villager
                return RANDOM.nextInt(3) == 0 ?
                        new ZombieVillager(EntityType.ZOMBIE_VILLAGER, world) :
                        new Zombie(world);
            case 3:
                // Zombies with equipment
                Zombie armoredZombie = new Zombie(world);
                if (RANDOM.nextBoolean()) {
                    armoredZombie.setItemSlot(
                            EquipmentSlot.HEAD,
                            new ItemStack(Items.IRON_HELMET)
                    );
                }
                return armoredZombie;
            case 4:
                // Stronger zombies
                Zombie strongZombie = new Zombie(world);
                strongZombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
                strongZombie.setHealth(40.0F);
                return strongZombie;
            case 5:
                // Boss-like zombie
                Zombie bossZombie = new Zombie(world);
                bossZombie.setCustomName(Component.literal("Zombie Raid Boss"));
                bossZombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0D);
                bossZombie.setHealth(60.0F);
                bossZombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.0D);
                return bossZombie;
            default:
                return new Zombie(world);
        }
    }

    private static int calculateZombieCount(ServerLevel world, int wave) {
        // Scale zombie count based on world difficulty
        return switch (world.getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY -> wave * 2;
            case NORMAL -> wave * 3;
            case HARD -> wave * 4;
        };
    }

    private static BlockPos getRandomNearbyPos(BlockPos center) {
        // Spread zombies around the center
        return center.offset(
                RANDOM.nextInt(10) - 5,
                0,
                RANDOM.nextInt(10) - 5
        );
    }
}