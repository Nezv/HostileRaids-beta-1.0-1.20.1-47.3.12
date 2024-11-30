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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Mod.EventBusSubscriber(modid = HostileRaids.MODID)
public class ModEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModEvents.class);
    private static final Random RANDOM = new Random();
    private static Map<UUID, Boolean> raidInProgress = new HashMap<>();
    private static Map<UUID, ZombieRaidData> activeRaids = new HashMap<>();

    private static class ZombieRaidData {
        int currentWave = 1;
        Set<Zombie> currentWaveZombies = new HashSet<>();
        BlockPos raidCenter;
        ServerLevel world;
        Player player;
        boolean isRaidActive = false;

        ZombieRaidData(ServerLevel world, BlockPos center, Player player) {
            this.world = world;
            this.raidCenter = center;
            this.player = player;
        }
    }

    public static void register() {
        // Register the events to the Forge event bus
        MinecraftForge.EVENT_BUS.register(ModEvents.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only trigger on server side
        if (!event.player.level().isClientSide()) {
            UUID playerUUID = event.player.getUUID();

            // Check if raid is not already in progress and player has rotten flesh
            if (!raidInProgress.getOrDefault(playerUUID, false) &&
                    event.player.getInventory().contains(new ItemStack(Items.ROTTEN_FLESH))) {

                // Trigger raid at player's location
                triggerZombieRaid(event.player.level(), event.player.blockPosition(), event.player);

                // Mark raid as in progress for this player
                raidInProgress.put(playerUUID, true);
            }

            // Check for wave completion if a raid is in progress
            ZombieRaidData raidData = activeRaids.get(playerUUID);
            if (raidData != null && raidData.isRaidActive) {
                checkWaveCompletion(raidData);
            }
        }
    }



    public static void triggerZombieRaid(Level world, BlockPos center, Player player) {
        // Ensure we're in a server world and can support raids
        if (!(world instanceof ServerLevel serverLevel) ||
                world.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        // Create raid data for this player
        ZombieRaidData raidData = new ZombieRaidData(serverLevel, center, player);
        activeRaids.put(player.getUUID(), raidData);

        // Start the first wave
        startNextWave(raidData);

        // Play raid start sound
        world.playSound(null, center, SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // Send start message
        player.sendSystemMessage(Component.literal("Zombie Raid Started! Wave 1"));
    }

    private static void startNextWave(ZombieRaidData raidData) {
        // Check if raid is complete
        if (raidData.currentWave > 5) {
            endRaid(raidData);
            return;
        }

        // Clear previous wave zombies
        raidData.currentWaveZombies.clear();

        // Spawn new wave
        int zombieCount = calculateZombieCount(raidData.world, raidData.currentWave);
        LOGGER.info("Starting wave " + raidData.currentWave + " with " + zombieCount + " zombies.");

        for (int i = 0; i < zombieCount; i++) {
            Zombie zombie = createSpecialZombie(raidData.world, raidData.currentWave);

            // Position with some randomness
            BlockPos spawnPos = getRandomNearbyPos(raidData.raidCenter);
            zombie.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

            // Track this zombie in current wave
            raidData.currentWaveZombies.add(zombie);
            raidData.world.addFreshEntity(zombie);
        }

        raidData.isRaidActive = true;

        // Call checkWaveCompletion to ensure the next wave starts if the current wave is defeated
        checkWaveCompletion(raidData);
    }

    public static void endRaid(ZombieRaidData raidData) {
        raidData.player.sendSystemMessage(Component.literal("Zombie Raid Completed!"));
        activeRaids.remove(raidData.player.getUUID());
        raidInProgress.put(raidData.player.getUUID(), false); // Reset raid status
    }

    private static void checkWaveCompletion(ZombieRaidData raidData) {
        // Remove any defeated zombies from the set
        raidData.currentWaveZombies.removeIf(zombie -> !zombie.isAlive());

        // If no zombies left in current wave, start next wave
        if (raidData.currentWaveZombies.isEmpty()) {
            raidData.currentWave++;
            raidData.player.sendSystemMessage(Component.literal("Wave " + raidData.currentWave + " incoming!"));
            startNextWave(raidData);
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
                    armoredZombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET)
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