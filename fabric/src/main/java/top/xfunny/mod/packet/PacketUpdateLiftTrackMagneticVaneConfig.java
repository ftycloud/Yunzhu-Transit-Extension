package top.xfunny.mod.packet;

import org.mtr.mapping.holder.BlockEntity;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.MinecraftServer;
import org.mtr.mapping.holder.ServerPlayerEntity;
import org.mtr.mapping.registry.PacketHandler;
import org.mtr.mapping.tool.PacketBufferReceiver;
import org.mtr.mapping.tool.PacketBufferSender;
import top.xfunny.mod.Init;
import top.xfunny.mod.block.LiftTrackMagneticVane;

public final class PacketUpdateLiftTrackMagneticVaneConfig extends PacketHandler {

    private final BlockPos blockPos;
    private final String floorNumber;
    private final String floorDescription;

    public PacketUpdateLiftTrackMagneticVaneConfig(PacketBufferReceiver packetBufferReceiver) {
        blockPos = BlockPos.fromLong(packetBufferReceiver.readLong());
        floorNumber = packetBufferReceiver.readString();
        floorDescription = packetBufferReceiver.readString();
    }

    public PacketUpdateLiftTrackMagneticVaneConfig(BlockPos blockPos, String floorNumber, String floorDescription) {
        this.blockPos = blockPos;
        this.floorNumber = floorNumber;
        this.floorDescription = floorDescription;
    }

    @Override
    public void write(PacketBufferSender packetBufferSender) {
        packetBufferSender.writeLong(blockPos.asLong());
        packetBufferSender.writeString(floorNumber);
        packetBufferSender.writeString(floorDescription);
    }

    @Override
    public void runServer(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity) {
        if (!Init.isChunkLoaded(serverPlayerEntity.getEntityWorld(), blockPos)) {
            return;
        }

        final BlockEntity entity = serverPlayerEntity.getEntityWorld().getBlockEntity(blockPos);
        if (entity != null && entity.data instanceof LiftTrackMagneticVane.BlockEntity) {
            ((LiftTrackMagneticVane.BlockEntity) entity.data).setData(floorNumber, floorDescription);
        }
    }
}
