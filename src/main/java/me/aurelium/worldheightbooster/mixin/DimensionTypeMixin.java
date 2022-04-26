package me.aurelium.worldheightbooster.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
	/**
	 * @author AutumnAurelium
	 * @reason This is probably not the ideal solution here.
	 */
	@Overwrite
	private static DataResult<DimensionType> checkHeight(DimensionType type) {
		if (type.getHeight() < 16) {
			return DataResult.error("height has to be at least 16");
		} else if (type.getLogicalHeight() > type.getHeight()) {
			return DataResult.error("logical_height cannot be higher than height");
		} else if (type.getHeight() % 16 != 0) {
			return DataResult.error("height has to be multiple of 16");
		} else {
			return type.getMinimumY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(type);
		}
	}
}
