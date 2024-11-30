package net.nez.hostileraids;

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

public class RaidTriggerFleshItem extends Item {
    public RaidTriggerFleshItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.is(Items.ROTTEN_FLESH) && !world.isClientSide()) {
            Vec3 playerPos = player.position();
            BlockPos blockPos = BlockPos.containing(playerPos);

            try {
                ModEvents.triggerZombieRaid(world, blockPos, player);

                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                return InteractionResultHolder.success(itemstack);
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("Failed to trigger raid: " + e.getMessage()));
                return InteractionResultHolder.fail(itemstack);
            }
        }

        return InteractionResultHolder.consume(itemstack);
    }
}