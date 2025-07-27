package top.xfunny.mod.block;

import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.BlockEntityExtension;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mapping.tool.HolderBase;
import org.mtr.mod.block.IBlock;
import top.xfunny.mod.BlockEntityTypes;
import top.xfunny.mod.block.base.LiftButtonsBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ThyssenkruppTEGL1Button1 extends LiftButtonsBase {
    public ThyssenkruppTEGL1Button1() {
        super(true, true, 0.3);
    }

    @Nonnull
    @Override
    public VoxelShape getOutlineShape2(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        final boolean single = !IBlock.getStatePropertySafe(world.getBlockState(pos), SINGLE);
        return IBlock.getVoxelShapeByDirection(single ? 5.7 : 6.45, 0.675, 0, single ? 10.3 : 9.55, 9.875, 0.5, IBlock.getStatePropertySafe(state, FACING));
    }

    @Nonnull
    @Override
    public BlockEntityExtension createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntity(blockPos, blockState);
    }

    @Override
    public void addBlockProperties(List<HolderBase<?>> properties) {
        properties.add(FACING);
        properties.add(UNLOCKED);
        properties.add(SINGLE);
    }

    public void addTooltips(@NotNull ItemStack stack, @Nullable BlockView world, List<MutableText> tooltip, @NotNull TooltipContext options) {
        tooltip.add(TextHelper.translatable("tooltip.warning_block_testing").formatted(TextFormatting.RED));
    }

    public static class BlockEntity extends BlockEntityBase {
        public BlockEntity(BlockPos pos, BlockState state) {
            super(BlockEntityTypes.THYSSENKRUPP_TEGL1_BUTTON_1.get(), pos, state);
        }
    }
}
