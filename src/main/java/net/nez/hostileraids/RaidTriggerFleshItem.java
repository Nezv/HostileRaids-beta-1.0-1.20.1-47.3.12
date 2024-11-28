package net.nez.hostileraids;

// Various Minecraft imports for item interaction
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;

// An alternative (now unused) method of triggering raids via item use
public class RaidTriggerFleshItem extends Item {
    public RaidTriggerFleshItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Check if the item is rotten flesh and we're on the server side
        if (itemstack.is(Items.ROTTEN_FLESH) && !world.isClientSide()) {
            // Get the player's position
            Vec3 playerPos = player.position();

            // Convert to BlockPos
            BlockPos blockPos = BlockPos.containing(playerPos);

            try {
                // Trigger the zombie raid at the player's location
                ModEvents.triggerZombieRaid(world, blockPos);

                // Play a sound to indicate raid start
                world.playSound(null, blockPos, SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

                // Send a message to the player
                player.sendSystemMessage(Component.literal("A zombie raid has been triggered!"));

                // Consume the item (reduce stack size)
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                return InteractionResultHolder.success(itemstack);
            } catch (Exception e) {
                // Send an error message if something goes wrong
                player.sendSystemMessage(Component.literal("Failed to trigger raid: " + e.getMessage()));
                return InteractionResultHolder.fail(itemstack);
            }
        }

        return InteractionResultHolder.consume(itemstack);
    }
}