package me.aurelium.worldheightbooster.mixin;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPos.class)
public class BlockPosMixin {
	@Shadow @Mutable @Final
	private static int SIZE_BITS_X = 24;
	@Shadow @Mutable @Final
	private static int SIZE_BITS_Z = SIZE_BITS_X;
	@Shadow @Mutable @Final
	public static final int SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
	@Shadow @Mutable @Final
	private static final long BITS_X = (1L << SIZE_BITS_X) - 1L;
	@Shadow @Mutable @Final
	private static final long BITS_Y = (1L << SIZE_BITS_Y) - 1L;
	@Shadow @Mutable @Final
	private static final long BITS_Z = (1L << SIZE_BITS_Z) - 1L;
	@Shadow @Mutable @Final
	private static final int BIT_SHIFT_Y = 0;
	@Shadow @Mutable @Final
	private static final int BIT_SHIFT_Z = SIZE_BITS_Y;
	@Shadow @Mutable @Final
	private static final int BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;
}
