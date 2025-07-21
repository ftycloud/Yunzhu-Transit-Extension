package top.xfunny.mod.item;

import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.DirectionHelper;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mod.block.BlockLiftButtons;
import org.mtr.mod.block.BlockLiftPanelBase;
import org.mtr.mod.block.BlockLiftTrackBase;
import org.mtr.mod.block.BlockLiftTrackFloor;
import org.mtr.mod.item.ItemBlockClickingBase;
import top.xfunny.mod.ButtonRegistry;
import top.xfunny.mod.Init;
import top.xfunny.mod.LiftFloorRegistry;
import top.xfunny.mod.block.base.LiftButtonsBase;
import top.xfunny.mod.block.base.LiftDestinationDispatchTerminalBase;
import top.xfunny.mod.block.base.LiftPanelBase;

import static top.xfunny.mod.item.LinkerValidTypes.VALID_TYPES;

public class YteGroupLiftButtonsLinker extends YTEItemBlockClickingBase implements DirectionHelper {
    private final boolean isConnector;
    BlockPos posStart;
    int number;

    public YteGroupLiftButtonsLinker(boolean isConnector, ItemSettings itemSettings) {
        super(itemSettings.maxCount(1));
        this.isConnector = isConnector;
    }

    private static void connect(World world, BlockPos blockPos1, BlockPos blockPos2, boolean isAdd) {
        final BlockEntity blockEntity1 = world.getBlockEntity(blockPos1);
        final BlockEntity blockEntity2 = world.getBlockEntity(blockPos2);

        if (blockEntity1 != null && blockEntity2 != null) {
            if (blockEntity2.data instanceof BlockLiftTrackFloor.BlockEntity) {
                if (blockEntity1.data instanceof LiftFloorRegistry) {
                    ((LiftFloorRegistry) blockEntity1.data).registerFloor(blockPos1, world, blockPos2, isAdd);
                } else if (blockEntity1.data instanceof BlockLiftButtons.BlockEntity) {
                    ((BlockLiftButtons.BlockEntity) blockEntity1.data).registerFloor(blockPos2, isAdd);
                } else if (blockEntity1.data instanceof BlockLiftPanelBase.BlockEntityBase) {
                    ((BlockLiftPanelBase.BlockEntityBase) blockEntity1.data).registerFloor(world, blockPos2, isAdd);
                }
            } else if (blockEntity2.data instanceof LiftButtonsBase.BlockEntityBase || blockEntity2.data instanceof LiftDestinationDispatchTerminalBase.BlockEntityBase) {
                if(blockEntity1.data instanceof LiftFloorRegistry){
                    ((ButtonRegistry) blockEntity1.data).registerButton(world, blockPos2, isAdd);
                }
            }
        }
    }

    @Override
    protected void onStartClick(@NotNull ItemUsageContext context, @NotNull CompoundTag compoundTag) {
    }

    @Override
    protected void onEndClick(ItemUsageContext context, @NotNull BlockPos posEnd, @NotNull CompoundTag compoundTag) {
        final PathFinder pathFinder = new PathFinder();
        final PlayerEntity playerEntity = context.getPlayer();
        int floorCount = 0;
        final World world = context.getWorld();
        posStart = context.getBlockPos();
        number = 0;

        while (true) {
            if (posStart != null && (world.getBlockState(posStart).getBlock().data instanceof BlockLiftTrackBase ||
                    world.getBlockState(posEnd).getBlock().data instanceof BlockLiftTrackBase)) {
                connect(world, posStart, posEnd, isConnector);
                connect(world, posEnd, posStart, isConnector);
                Object[] pos = pathFinder.findPath(context, posStart, posEnd);
                posStart = (BlockPos) pos[0];
                posEnd = (BlockPos) pos[1];

                if (world.getBlockState(posEnd).getBlock().data instanceof LiftButtonsBase ||
                        world.getBlockState(posEnd).getBlock().data instanceof LiftDestinationDispatchTerminalBase ||
                        world.getBlockState(posEnd).getBlock().data instanceof LiftPanelBase ||
                        world.getBlockState(posEnd).getBlock().data instanceof BlockLiftButtons ||
                        world.getBlockState(posEnd).getBlock().data instanceof BlockLiftPanelBase
                ) {
                    floorCount++;
                }

            }else if(world.getBlockState(posStart).getBlock().data instanceof BlockLiftTrackBase ||
                    world.getBlockState(posEnd).getBlock().data instanceof BlockLiftTrackBase){
                if (playerEntity != null) {
                    playerEntity.sendMessage(isConnector ? Text.cast(TextHelper.translatable("message.linker_status_failed")) : Text.cast(TextHelper.translatable("message.linker_status_failed_remove")), true);
                }
                break;
            }else {
                if (playerEntity != null) {//需要第三点
                    playerEntity.sendMessage(Text.cast(TextHelper.translatable("message.floor_auto_setter_status_need_track_floor_position")), true);
                }
                break;
            }
            if (number == pathFinder.getMark().size()) {
                if (playerEntity != null) {
                    playerEntity.sendMessage(isConnector ? Text.cast(TextHelper.translatable("message.linker_status_finished", floorCount)) : Text.cast(TextHelper.translatable("message.linker_status_finished_remove", floorCount)), true);
                }
                break;
            }
            number++;
        }
    }

    @Override
    public void onThirdClick(ItemUsageContext context,BlockPos pos1, BlockPos pos2, BlockPos pos3, CompoundTag compoundTag){
        final PlayerEntity playerEntity = context.getPlayer();
        int floorCount = 0;
        final PathFinder pathFinder = new PathFinder();
        final World world = context.getWorld();
        number = 0;

        while (true) {
            if(pos1 != null && pos2 != null && world.getBlockState(pos3).getBlock().data instanceof BlockLiftTrackBase){
                connect(world, pos1, pos2, isConnector);
                connect(world, pos2, pos1, isConnector);
                Object[] pos = pathFinder.findPath(context, pos1, pos2, pos3);
                pos1 = (BlockPos) pos[1];
                pos2 = (BlockPos) pos[2];
                pos3 = (BlockPos) pos[0];//轨道

                final Block blockEnd = world.getBlockState(pos2).getBlock();
                final Block blockStart = world.getBlockState(pos1).getBlock();
                final boolean isBlockEndValid = blockEnd.data instanceof LiftButtonsBase || blockEnd.data instanceof LiftDestinationDispatchTerminalBase || blockEnd.data instanceof LiftPanelBase;
                final boolean isBlockStartValid = blockStart.data instanceof LiftButtonsBase || blockStart.data instanceof LiftDestinationDispatchTerminalBase || blockStart.data instanceof LiftPanelBase;

                if (isBlockEndValid && isBlockStartValid) {
                    floorCount++;
                }
            }else {
                if (playerEntity != null) {
                    playerEntity.sendMessage(isConnector ? Text.cast(TextHelper.translatable("message.linker_status_failed")) : Text.cast(TextHelper.translatable("message.linker_status_failed_remove")), true);
                }
                break;
            }
            if (number == pathFinder.getMark().size()) {
                if (playerEntity != null) {
                    playerEntity.sendMessage(isConnector ? Text.cast(TextHelper.translatable("message.linker_status_finished", floorCount)) : Text.cast(TextHelper.translatable("message.linker_status_finished_remove", floorCount)), true);
                }
                break;
            }
            number++;
        }
    };

    private boolean isValidType(Object data) {
        return VALID_TYPES.contains(data.getClass());
    }

    @Override
    protected boolean clickCondition(ItemUsageContext context) {
        final Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        return isValidType(block.data);
    }
}