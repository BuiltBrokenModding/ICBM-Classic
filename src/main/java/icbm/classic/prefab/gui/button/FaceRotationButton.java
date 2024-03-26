package icbm.classic.prefab.gui.button;

import icbm.classic.lib.data.FaceRotations;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FaceRotationButton extends GuiButtonBase<FaceRotationButton> implements IGuiComponent, IToolTip {

    public static final Component TOOLTIP = new TextComponentTranslation("gui.icbmclassic:button.face.tooltip");


    // Icon this designed to cover is 13x13
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;

    private final Supplier<Direction> currentRotationGetter;
    private final Supplier<Direction> blockFaceGetter;
    private Direction prevSide;

    public FaceRotationButton(int buttonId, int x, int y, Supplier<Direction> blockFaceGetter, Supplier<Direction> currentRotationGetter, Consumer<Direction> setter, Runnable networkCall) {
        super(buttonId, x, y, WIDTH, HEIGHT, "-");
        this.blockFaceGetter = blockFaceGetter;
        this.currentRotationGetter = currentRotationGetter;
        this.setAction(() -> {
            setter.accept(rotate());
            networkCall.run();
        });
        this.setTooltip(() -> TOOLTIP);
    }

    protected Direction rotate() {
        final Direction currentRotation = currentRotationGetter.get();
        final Direction blockFace = blockFaceGetter.get();
        if (GuiContainer.isShiftKeyDown()) {
            return FaceRotations.rotateLeft(blockFace, currentRotation);
        }
        return FaceRotations.rotateRight(blockFace, currentRotation);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        final Direction side = currentRotationGetter.get();
        if (prevSide != side) {
            prevSide = side;
            //displayString = (side.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? "-" : "") + side.getAxis().getName().toUpperCase();
            displayString = side.name().toUpperCase().substring(0, 1);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return super.mousePressed(mc, mouseX, mouseY) && (GuiContainer.isAltKeyDown() || GuiContainer.isShiftKeyDown());
    }
}
