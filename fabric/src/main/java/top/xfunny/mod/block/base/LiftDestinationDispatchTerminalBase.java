package top.xfunny.mod.block.base;

import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftDirection;
import org.mtr.core.data.LiftInstruction;
import org.mtr.core.data.Position;
import org.mtr.core.operation.PressLift;
import org.mtr.core.tool.Vector;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.mtr.libraries.kotlin.Triple;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.*;
import org.mtr.mod.InitClient;
import org.mtr.mod.block.IBlock;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.item.ItemLiftRefresher;
import org.mtr.mod.packet.PacketPressLiftButton;
import top.xfunny.mixin.MixinLiftSchema;
import top.xfunny.mod.ButtonRegistry;
import top.xfunny.mod.Init;
import top.xfunny.mod.LiftFloorRegistry;
import top.xfunny.mod.LiftLanternController;
import top.xfunny.mod.keymapping.DefaultButtonsKeyMapping;
import top.xfunny.mod.util.GetLiftDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mtr.core.data.LiftDirection.NONE;

public abstract class LiftDestinationDispatchTerminalBase extends BlockExtension implements DirectionHelper, BlockWithEntity, IBlock {
    public static final BooleanProperty UNLOCKED = BooleanProperty.of("unlocked");
    private final boolean isOdd;

    public LiftDestinationDispatchTerminalBase(boolean isOdd) {
        super(BlockHelper.createBlockSettings(true, true));
        this.isOdd = isOdd;
    }

    public static void hasButtonsClient(BlockPos trackPosition, FloorLiftCallback callback) {
        MinecraftClientData.getInstance().lifts.forEach(lift -> {
            // 获取电梯轨道位置对应的楼层索引
            final int floorIndex = lift.getFloorIndex(Init.blockPosToPosition(trackPosition));

            // 如果楼层索引非负，表示电梯中存在该楼层，执行回调函数
            if (floorIndex >= 0) {
                callback.accept(floorIndex, lift);
            }
        });
    }

    @Nonnull
    @Override
    public abstract ActionResult onUse2(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit);

    @Override
    public BlockState getPlacementState2(ItemPlacementContext ctx) {
        // 获取玩家面对的方向
        final Direction facing = ctx.getPlayerFacing();
        // 根据默认状态和玩家面对的方向来设置方块状态，并返回
        if (!isOdd) {
            return IBlock.isReplaceable(ctx, facing.rotateYClockwise(), 2) ? getDefaultState2().with(new Property<>(FACING.data), facing.data).with(new Property<>(SIDE.data), EnumSide.LEFT) : null;
        } else {
            return getDefaultState2().with(new Property<>(FACING.data), facing.data);
        }
    }

    @Override
    public void onPlaced2(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient()) {
            final Direction facing = IBlock.getStatePropertySafe(state, FACING);
            if (!isOdd) {
                world.setBlockState(pos.offset(facing.rotateYClockwise()), getDefaultState2().with(new Property<>(FACING.data), facing.data).with(new Property<>(SIDE.data), EnumSide.RIGHT), 3);
            }
            world.updateNeighbors(pos, Blocks.getAirMapped());
            state.updateNeighbors(new WorldAccess(world.data), pos, 3);
        }
    }

    @Override
    public void onBreak2(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!isOdd) {
            if (IBlock.getStatePropertySafe(state, SIDE) == EnumSide.RIGHT) {
                IBlock.onBreakCreative(world, player, pos.offset(IBlock.getSideDirection(state)));
            } else if (IBlock.getStatePropertySafe(state, SIDE) == EnumSide.LEFT) {
                IBlock.onBreakCreative(world, player, pos.offset(IBlock.getSideDirection(state)));
            }
        }
        super.onBreak2(world, pos, state, player);
    }

    @FunctionalInterface
    public interface FloorLiftCallback {
        void accept(int floor, Lift lift);
    }

    public static class BlockEntityBase extends BlockEntityExtension implements LiftFloorRegistry, ButtonRegistry, LiftLanternController {
        // 用于在CompoundTag中标识地板位置数组的键
        private static final String KEY_TRACK_FLOOR_POS = "track_floor_pos";
        private static final String KEY_LIFT_BUTTON_POSITIONS = "lift_button_position";
        private static final String KEY_SCREEN_ID = "screen_id";
        public final ObjectOpenHashSet<BlockPos> liftButtonPositions = new ObjectOpenHashSet<>();
        private final LinkedHashSet<BlockPos> trackPositions = new LinkedHashSet<>();
        public LiftDirection liftDirection = NONE;
        public BlockPos selfPos;
        private DefaultButtonsKeyMapping keyMapping = new DefaultButtonsKeyMapping();
        private String screenId;

        private char liftIdentifier;

        public BlockEntityBase(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
            super(type, blockPos, blockState);
        }

        public DefaultButtonsKeyMapping getKeyMapping() {
            return keyMapping;
        }

        public void setKeyMapping(DefaultButtonsKeyMapping keyMapping) {
            this.keyMapping = keyMapping;
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            // 清空当前位置集合，准备加载新的数据
            trackPositions.clear();
            liftButtonPositions.clear();

            // 从CompoundTag中读取名为KEY_TRACK_FLOOR_POS的长整型数组
            // 每个长整型代表一个BlockPos位置，将其转换并添加到trackPositions集合中
            for (final long position : compoundTag.getLongArray(KEY_TRACK_FLOOR_POS)) {
                trackPositions.add(BlockPos.fromLong(position));
            }

            for (final long position : compoundTag.getLongArray(KEY_LIFT_BUTTON_POSITIONS)) {
                liftButtonPositions.add(BlockPos.fromLong(position));
            }
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            // 创建一个临时的List，用于存储trackPositions的长整型表示
            final List<Long> trackPositionsList = new ArrayList<>();

            // 遍历trackPositions集合，将每个位置转换为长整型并添加到trackPositionsList中
            // 这里的转换是为了以长整型数组的形式存储这些位置信息
            trackPositions.forEach(position -> {
                trackPositionsList.add(position.asLong());
            });
            // 将收集到的trackPositions长整型列表以数组的形式存储到compoundTag中
            // 使用的键是KEY_TRACK_FLOOR_POS，值是trackPositionsList数组
            compoundTag.putLongArray(KEY_TRACK_FLOOR_POS, trackPositionsList);


            final List<Long> liftButtonPositionsList = new ArrayList<>();
            liftButtonPositions.forEach(position -> {
                liftButtonPositionsList.add(position.asLong());
            });
            compoundTag.putLongArray(KEY_LIFT_BUTTON_POSITIONS, liftButtonPositionsList);

            compoundTag.putString(KEY_SCREEN_ID, screenId);
        }

        public void registerFloor(BlockPos selfPos, World world, BlockPos pos, boolean isAdd) {
            this.selfPos = selfPos;
            if (IBlock.getStatePropertySafe(world, getPos2(), SIDE) == EnumSide.RIGHT) {
                final BlockEntity blockEntity = world.getBlockEntity(getPos2().offset(IBlock.getStatePropertySafe(world, getPos2(), FACING).rotateYCounterclockwise()));
                if (blockEntity != null && blockEntity.data instanceof LiftButtonsBase.BlockEntityBase) {
                    ((LiftButtonsBase.BlockEntityBase) blockEntity.data).registerFloor(selfPos, world, pos, isAdd);
                }
            } else {
                if (isAdd) {
                    // 如果是添加操作，则将位置添加到跟踪列表中
                    trackPositions.add(pos);
                } else {
                    // 如果是非添加操作，则从跟踪列表中移除该位置
                    trackPositions.remove(pos);
                }
            }
            // 更新数据状态，标记数据为“脏”，表示需要保存或同步
            markDirty2();
        }

        @Override
        public void registerButton(World world, BlockPos blockPos, boolean isAdd) {

            if (IBlock.getStatePropertySafe(world, getPos2(), SIDE) == EnumSide.RIGHT) {
                final BlockEntity blockEntity = world.getBlockEntity(getPos2().offset(IBlock.getStatePropertySafe(world, getPos2(), FACING).rotateYCounterclockwise()));
                if (blockEntity != null && blockEntity.data instanceof LiftButtonsBase.BlockEntityBase) {
                    ((LiftButtonsBase.BlockEntityBase) blockEntity.data).registerButton(world, blockPos, isAdd);
                }
            } else {
                if (isAdd) {
                    // 如果是添加操作，则将位置添加到跟踪列表中
                    liftButtonPositions.add(blockPos);
                } else {
                    // 如果是非添加操作，则从跟踪列表中移除该位置
                    liftButtonPositions.remove(blockPos);
                }
            }
            markDirty2();
        }

        public void registerScreenId(String screenId) {
            this.screenId = screenId;
        }

        public String getScreenId() {
            return screenId;
        }

        @Override
        public ObjectOpenHashSet<BlockPos> getTrackPositions() {
            return new ObjectOpenHashSet<>(trackPositions);
        }

        public void forEachTrackPosition(Consumer<BlockPos> consumer) {
            trackPositions.forEach(consumer);
        }

        // ======================== 代价函数参数 ========================
        /** 电梯每运行一层的基础代价 */
        private static final double COST_PER_FLOOR = 1.0;
        /** 每次停站（开关门+加减速）的额外代价 */
        private static final double STOP_COST = 4.0;
        /** 折返（改变运行方向）的代价 */
        private static final double DIRECTION_CHANGE_COST = 8.0;
        /** 方向不匹配时的软惩罚（替代硬过滤） */
        private static final double DIRECTION_PENALTY = 20.0;
        /** 批量分组判定阈值：目的地与已有停站相差 ≤ 此值视为"邻近" */
        private static final int BATCH_THRESHOLD = 2;
        /** 批量分组基础奖励值 */
        private static final double BATCH_BASE_BONUS = 5.0;

        /**
         * 统一调度入口：对每台可到达目的地的电梯计算综合代价，选最低的推荐给乘客。
         * <p>
         * 代价 = pickupCost（到达召唤层）+ deliveryCost（送达目的地）
         *      + directionPenalty（方向惩罚，软约束）
         *      - batchBonus（批量分组奖励）
         */
        public String callLift(World world, BlockPos pos, String destination) {
            final BlockEntity blockEntity = world.getBlockEntity(pos);
            final BlockEntityBase data = (BlockEntityBase) blockEntity.data;

            // ---- Step 1: 收集所有候选电梯 ----
            // (trackPosition, lift, callingFloorIndex, destFloorIndex, destPosition, label)
            ObjectArrayList<LiftCandidate> candidates = new ObjectArrayList<>();
            final int[] counter = {0};

            trackPositions.forEach(trackPosition -> {
                final char label = (char) ('A' + counter[0]);
                counter[0]++;

                MinecraftClientData.getInstance().lifts.forEach(lift -> {
                    final int callingFloorIdx = lift.getFloorIndex(Init.blockPosToPosition(trackPosition));
                    if (callingFloorIdx < 0) return;

                    final Position destPos = locateFloor(world, lift, destination);
                    if (destPos == null) return;

                    final int destFloorIdx = lift.getFloorIndex(destPos);
                    candidates.add(new LiftCandidate(trackPosition, label, lift, callingFloorIdx, destFloorIdx, destPos));
                });
            });

            if (candidates.isEmpty()) {
                liftIdentifier = '?';
                return "?";
            }

            // ---- Step 2: 用统一代价函数评估所有候选 ----
            LiftCandidate bestCandidate = null;
            double bestCost = Double.MAX_VALUE;

            for (LiftCandidate candidate : candidates) {
                final double cost = calculateLiftCost(candidate);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestCandidate = candidate;
                }
            }

            // ---- Step 3: 执行 ----
            if (bestCandidate == null) {
                liftIdentifier = '?';
                return "?";
            }

            final BlockPos confirmTrackPosition = bestCandidate.trackPosition;
            final Position finalDestPosition = bestCandidate.destPosition;
            final LiftDirection neededDirection = determineDirection(
                    bestCandidate.callingFloorIndex, bestCandidate.destFloorIndex);
            this.liftDirection = neededDirection;
            liftIdentifier = bestCandidate.label;

            final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            hasButtonsClient(confirmTrackPosition, (floor, lift) -> {
                if (lift.getDoorValue() == 0) {
                    final PressLift pressLift = new PressLift();
                    pressLift.add(Init.blockPosToPosition(confirmTrackPosition), data.liftDirection);
                    InitClient.REGISTRY_CLIENT.sendPacketToServer(new PacketPressLiftButton(pressLift));
                }
            });

            scheduler.schedule(() -> {
                liftButtonPositions.forEach(lanternPos -> {
                    BlockEntity lanternBlockEntity = world.getBlockEntity(lanternPos);
                    if (lanternBlockEntity != null && lanternBlockEntity.data instanceof LiftButtonsBase.BlockEntityBase) {
                        LiftButtonsBase.BlockEntityBase lanternData = (LiftButtonsBase.BlockEntityBase) lanternBlockEntity.data;
                        lanternData.setPressedButtonDirection(data.liftDirection);
                    }
                });

                final PressLift pressLift1 = new PressLift();
                pressLift1.add(finalDestPosition, data.liftDirection);
                InitClient.REGISTRY_CLIENT.sendPacketToServer(new PacketPressLiftButton(pressLift1));
            }, 2, TimeUnit.SECONDS);

            return String.valueOf(liftIdentifier);
        }

        /**
         * 计算单台电梯的综合调度代价（越低越好）。
         */
        private double calculateLiftCost(LiftCandidate c) {
            final Lift lift = c.lift;
            final int callingIdx = c.callingFloorIndex;
            final int destIdx = c.destFloorIndex;

            final int currentFloorIdx = lift.getFloorIndex(lift.getCurrentFloor().getPosition());
            final LiftDirection liftDirection = lift.getDirection();
            final ObjectArrayList<LiftInstruction> instructions = ((MixinLiftSchema) lift).getInstructions();

            final int currentDir = liftDirection == LiftDirection.UP ? 1
                    : (liftDirection == LiftDirection.DOWN ? -1 : 0);
            final int neededDir = callingIdx < destIdx ? 1 : (callingIdx > destIdx ? -1 : 0);

            // 1) pickupCost: 从电梯当前位置到达召唤层的估算代价
            final double pickupCost = estimateTravelCost(currentFloorIdx, callingIdx, instructions, currentDir);

            // 2) deliveryCost: 从召唤层到目的地的纯楼层代价
            final double deliveryCost = Math.abs(destIdx - callingIdx) * COST_PER_FLOOR;

            // 3) directionPenalty: 软约束（替代硬过滤）
            final double directionPenalty =
                    (currentDir != 0 && neededDir != 0 && currentDir != neededDir)
                            ? DIRECTION_PENALTY : 0.0;

            // 4) batchBonus: 目的地与已有停站楼层邻近 → 倾向合并
            double batchBonus = 0.0;
            for (LiftInstruction inst : instructions) {
                final int stopFloor = inst.getFloor();
                final int dist = Math.abs(stopFloor - destIdx);
                if (dist == 0) {
                    batchBonus += BATCH_BASE_BONUS * 1.5;  // 同一楼层，最大奖励
                } else if (dist <= BATCH_THRESHOLD) {
                    batchBonus += BATCH_BASE_BONUS * (1.0 - (double) dist / (BATCH_THRESHOLD + 1));
                }
            }

            return pickupCost + deliveryCost + directionPenalty - batchBonus;
        }

        /**
         * 估算电梯从 fromIdx 到达 toIdx 的运行代价。
         * <p>
         * instructions 已按执行顺序排列（由 MTR 的 pressButton 保证），电梯总是先执行完
         * 当前方向上的所有停站，再折返。
         */
        private double estimateTravelCost(int fromIdx, int toIdx,
                                           ObjectArrayList<LiftInstruction> instructions,
                                           int currentDir) {
            if (fromIdx == toIdx) return 0.0;

            final int neededDir = toIdx > fromIdx ? 1 : -1;

            // 空闲或无指令：直接按楼层差估算
            if (currentDir == 0 || instructions.isEmpty()) {
                return Math.abs(toIdx - fromIdx) * COST_PER_FLOOR;
            }

            // 找到当前方向上的最远端停站
            int furthestInDir = fromIdx;
            for (LiftInstruction inst : instructions) {
                final int floor = inst.getFloor();
                if (currentDir == 1 && floor > furthestInDir) furthestInDir = floor;
                if (currentDir == -1 && floor < furthestInDir) furthestInDir = floor;
            }

            if (currentDir == neededDir) {
                // 同向：判断是否还没路过召唤层
                final boolean passedCalling =
                        (currentDir == 1 && fromIdx > toIdx) || (currentDir == -1 && fromIdx < toIdx);

                if (!passedCalling) {
                    final int floors = Math.abs(toIdx - fromIdx);
                    final int stops = countStopsBetween(fromIdx, toIdx, instructions, currentDir);
                    return floors * COST_PER_FLOOR + stops * STOP_COST;
                }
            }

            // 反向或已路过：先走到最远端 → 折返 → 再到召唤层
            final int floorsToFurthest = Math.abs(furthestInDir - fromIdx);
            final int stopsToFurthest = countStopsBetween(fromIdx, furthestInDir, instructions, currentDir);
            final int floorsBack = Math.abs(toIdx - furthestInDir);

            return floorsToFurthest * COST_PER_FLOOR
                    + stopsToFurthest * STOP_COST
                    + floorsBack * COST_PER_FLOOR
                    + DIRECTION_CHANGE_COST;
        }

        /**
         * 统计在 [from, to] 区间内（含端点）且方向与 dir 一致的停站数。
         */
        private int countStopsBetween(int from, int to, ObjectArrayList<LiftInstruction> instructions, int dir) {
            int count = 0;
            for (LiftInstruction inst : instructions) {
                final int floor = inst.getFloor();
                if (dir == 1 && floor > from && floor <= to) count++;
                if (dir == -1 && floor < from && floor >= to) count++;
            }
            return count;
        }

        /**
         * 候选电梯数据容器。
         */
        private static class LiftCandidate {
            final BlockPos trackPosition;
            final char label;
            final Lift lift;
            final int callingFloorIndex;
            final int destFloorIndex;
            final Position destPosition;

            LiftCandidate(BlockPos trackPosition, char label, Lift lift,
                          int callingFloorIndex, int destFloorIndex, Position destPosition) {
                this.trackPosition = trackPosition;
                this.label = label;
                this.lift = lift;
                this.callingFloorIndex = callingFloorIndex;
                this.destFloorIndex = destFloorIndex;
                this.destPosition = destPosition;
            }
        }

        public Position locateFloor(World world, Lift lift, String destination) {
            final Position[] foundPosition = new Position[1];

            lift.iterateFloors(liftFloor -> {
                String floorNumber = GetLiftDetails.getLiftDetails(
                        world,
                        lift,
                        Init.positionToBlockPos(liftFloor.getPosition())
                ).right().left();

                // 比较目标楼层和当前楼层号
                if (destination.equals(floorNumber)) {
                    foundPosition[0] = liftFloor.getPosition();
                }
            });
            return foundPosition[0];
        }

        public String getLiftIdentifier() {
            return String.valueOf(liftIdentifier);

        }

        public LiftDirection determineDirection(int currentFloorNumber, int destinationFloorNumber) {
            if (currentFloorNumber < destinationFloorNumber) {
                return LiftDirection.UP;
            } else if (currentFloorNumber > destinationFloorNumber) {
                return LiftDirection.DOWN;
            } else {
                return NONE;
            }
        }

        public void forEachLiftButtonPosition(Consumer<BlockPos> consumer) {
            liftButtonPositions.forEach(consumer);
        }

        public ObjectOpenHashSet<BlockPos> getLiftButtonPositions() {
            return liftButtonPositions;
        }

        public LiftDirection getPressedButtonDirection() {

            return this.liftDirection;
        }
    }
}
