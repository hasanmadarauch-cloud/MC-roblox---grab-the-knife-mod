package com.knifemod;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Kontroller:
 *  Sol tık                 -> hızlı kesme
 *  Sağ tık basılı tut/bırak -> bıçak fırlat
 *  Shift + sağ tık (mob/oyuncu) -> yakala
 *  Yakalıyken sağ tık       -> bıçakla
 *  Yakalıyken Shift + sağ tık -> fırlat (savur)
 */
public class KnifeItem extends SwordItem {
	public static final String GRAB_TAG = "KnifeGrab";
	private static final int THROW_COOLDOWN = 12;
	private static final int STAB_COOLDOWN = 7;

	public KnifeItem(Settings settings) {
		// Demir: 2 + 3 + 1 = 6 hasar, saldırı hızı 2.2 (kılıçtan hızlı)
		super(ToolMaterials.IRON, 3, -1.8f, settings);
	}

	public static boolean isGrabbing(ItemStack stack) {
		return stack.hasNbt() && stack.getNbt().getBoolean(GRAB_TAG);
	}

	// ---------- Sol tık: kesme ----------
	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target.getWorld() instanceof ServerWorld sw) {
			Blood.splash(sw, target, 10);
		}
		return super.postHit(stack, target, attacker);
	}

	// ---------- Shift + sağ tık: yakalama ----------
	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (hand != Hand.MAIN_HAND || !user.isSneaking() || isGrabbing(stack)) return ActionResult.PASS;
		if (user.getWorld().isClient) return ActionResult.SUCCESS;
		if (user instanceof ServerPlayerEntity sp && GrabManager.grab(sp, entity)) {
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	// ---------- Sağ tık: fırlatma / bıçaklama ----------
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (isGrabbing(stack)) {
			if (!world.isClient && user instanceof ServerPlayerEntity sp) {
				if (user.isSneaking()) GrabManager.fling(sp);
				else GrabManager.stab(sp);
			}
			user.getItemCooldownManager().set(this, STAB_COOLDOWN);
			return TypedActionResult.success(stack, world.isClient);
		}

		user.setCurrentHand(hand);
		return TypedActionResult.consume(stack);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.SPEAR;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof PlayerEntity player)) return;
		int charge = getMaxUseTime(stack) - remainingUseTicks;
		if (charge < 4) return;

		float power = Math.min(charge / 10.0f, 1.0f); // 10 tikte (0.5 sn) tam güç
		if (!world.isClient) {
			ThrownKnifeEntity knife = new ThrownKnifeEntity(world, player);
			knife.setItem(stack.copyWithCount(1));
			knife.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 1.2f + power * 1.8f, 0.4f);
			world.spawnEntity(knife);
			if (!player.getAbilities().creativeMode) {
				stack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));
			}
		}
		world.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 0.8f, 1.6f);
		player.getItemCooldownManager().set(this, THROW_COOLDOWN);
	}

	// ---------- Envanter temizliği ----------
	@Override
	public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
		// Sunucu yeniden başlarsa vs. eski "yakalıyor" etiketi kalmasın
		if (!world.isClient && isGrabbing(stack) && entity instanceof PlayerEntity p && !GrabManager.isHolding(p)) {
			stack.removeSubNbt(GRAB_TAG);
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.knifemod.knife.tip1").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.knifemod.knife.tip2").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.knifemod.knife.tip3").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.knifemod.knife.tip4").formatted(Formatting.GRAY));
	}
}
