package icbm.classic.content.blocks.radarstation.data;

import icbm.classic.content.blocks.radarstation.TileRadarStation;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class RadarRenderData {

    public static final int UV_SIZE = 54;

    private final TileRadarStation host;

    @Getter
    private final List<RadarRenderDot> dots = new LinkedList<>();

    public RadarRenderData(TileRadarStation host) {
        this.host = host;
    }

    public void update() {


        addDot(this.host.getPos().getX() + 0.5, this.host.getPos().getZ() + 0.5, RadarDotType.MARKER);

        // Edge
        addDot(this.host.getDetectionRange() + this.host.getPos().getX() + 0.5, this.host.getPos().getZ() + 0.5, RadarDotType.MARKER);
        addDot(-this.host.getDetectionRange() + this.host.getPos().getX() + 0.5, this.host.getPos().getZ() + 0.5, RadarDotType.MARKER);
        addDot(this.host.getPos().getX() + 0.5, this.host.getPos().getZ() + 0.5 + this.host.getDetectionRange(), RadarDotType.MARKER);
        addDot(this.host.getPos().getX() + 0.5, this.host.getPos().getZ() + 0.5 - this.host.getDetectionRange(), RadarDotType.MARKER);

        // Trigger
        addDot(this.host.getPos().getX() + 0.5 + this.host.getTriggerRange(), this.host.getPos().getZ() + 0.5, RadarDotType.MARKER);
        addDot(this.host.getPos().getX() + 0.5 - this.host.getTriggerRange(), this.host.getPos().getZ() + 0.5, RadarDotType.MARKER);
        addDot(this.host.getPos().getX() + 0.5, this.host.getPos().getZ() + 0.5 + this.host.getTriggerRange(), RadarDotType.MARKER);
        addDot(this.host.getPos().getX() + 0.5, this.host.getPos().getZ() + 0.5 - this.host.getTriggerRange(), RadarDotType.MARKER);

        this.host.getDetectedThreats().forEach((entity) ->  addDot(entity.posX, entity.posZ, RadarDotType.HOSTILE));
        this.host.getIncomingThreats().forEach((entity) ->  addDot(entity.x(), entity.z(), RadarDotType.INCOMING));
    }

    public void addDot(double ex, double ez, RadarDotType type) {
        final double deltaX = ex - (this.host.getPos().getX() + 0.5);
        final double deltaZ = ez - (this.host.getPos().getZ() + 0.5);

        final double vecX = deltaX / this.host.getDetectionRange();
        final double vecZ = deltaZ / this.host.getDetectionRange();

        final int x = (int)Math.floor(vecX * (UV_SIZE / 2f));
        final int z = (int)Math.floor(vecZ * (UV_SIZE / 2f));

        dots.add(new RadarRenderDot(x, z, type));
    }

    public void clear() {
        dots.clear();
    }

    public void setDots(List<RadarRenderDot> dots) {
        this.dots.clear();
        this.dots.addAll(dots);
    }

    public static List<RadarRenderDot> decodeDots(ByteBuf buf) {

        final List<RadarRenderDot> dots = new LinkedList();

        int dotCount = buf.readInt();
        for(int i = 0; i < dotCount; i++) {
            dots.add(new RadarRenderDot(buf.readInt(), buf.readInt(), RadarDotType.get(buf.readByte())));
        }
        return dots;
    }

    public static void encodeDots(ByteBuf buf, List<RadarRenderDot> dots) {
        buf.writeInt(dots.size());
        dots.forEach(dot -> {
            buf.writeInt(dot.getX());
            buf.writeInt(dot.getY());
            buf.writeByte(dot.getType().ordinal());
        });
    }
}
