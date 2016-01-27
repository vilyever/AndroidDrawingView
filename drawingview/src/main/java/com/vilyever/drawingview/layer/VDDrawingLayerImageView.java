package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.R;
import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.model.VDDrawingLayer;
import com.vilyever.drawingview.model.VDDrawingStep;
import com.vilyever.unitconversion.VDDimenConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 * image图层view，同步绘制，可任意对图层进行变换
 */
public class VDDrawingLayerImageView extends ImageView implements VDDrawingLayerViewProtocol {
    private final VDDrawingLayerImageView self = this;

    /**
     * 边框虚线向外扩张1dp，以免遮挡图形
     * 父view理应取消{@link android.view.ViewGroup#setClipChildren(boolean)}
     */
    public final static int BorderOffset = VDDimenConversion.dpToPixel(1);

    /**
     * 边框虚线间距
     */
    private static DashPathEffect FirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect SecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    /* #Constructors */
    public VDDrawingLayerImageView(Context context) {
        super(context);
        self.init();
    }

    /* Properties */
    /**
     * 当前绘制的所有step
     */
    private List<VDDrawingStep> drawnSteps;
    private VDDrawingLayerImageView setDrawnSteps(List<VDDrawingStep> drawnSteps) {
        this.drawnSteps = drawnSteps;
        return this;
    }
    public List<VDDrawingStep> getDrawnSteps() {
        if (drawnSteps == null) {
            drawnSteps = new ArrayList<>();
        }
        return drawnSteps;
    }

    /**
     * 当前正在绘制的step
     */
    private VDDrawingStep currentDrawingStep;
    private VDDrawingLayerImageView setCurrentDrawingStep(VDDrawingStep currentDrawingStep) {
        this.currentDrawingStep = currentDrawingStep;
        return this;
    }
    public VDDrawingStep getCurrentDrawingStep() {
        return currentDrawingStep;
    }

    /**
     * 边线绘制矩形
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private RectF borderRect;
    public RectF getBorderRect() {
        if (borderRect == null) {
            borderRect = new RectF();
        }
        return borderRect;
    }

    /**
     * 边线绘制paint
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Paint borderPaint;
    public Paint getBorderPaint() {
        if (borderPaint == null) {
            borderPaint = new Paint();
            borderPaint.setAntiAlias(true);
            borderPaint.setDither(true);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2);
            borderPaint.setShadowLayer(2, 0, 0, Color.DKGRAY);
        }
        return borderPaint;
    }

    /**
     * 边线绘制path
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Path borderPath;
    public Path getBorderPath() {
        if (borderPath == null) {
            borderPath = new Path();
        }
        return borderPath;
    }

    /* Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 绘制图形
         */
        if (self.getDrawnSteps().size() > 0) {
            // 默认第一step位绘制step，即创建图层的step同时进行绘画
            // 之后的步骤都只是对图层进行变换
            VDDrawingStep step = self.getDrawnSteps().get(0);

            // 绘制图形，平移图形到图层左上角
            step.getBrush().drawPath(canvas, step.getDrawingPath(), step.getDrawingState().newStateByJoin(VDBrush.DrawingPointerState.CalibrateToOrigin));

            // 图层在被操作时，绘制边框
            if (self.isHandling()) {
                self.getBorderRect().set(self.getLeft(), self.getTop(), self.getRight(), self.getBottom());
                self.getBorderRect().offsetTo(0, 0);
                self.getBorderRect().left -= BorderOffset;
                self.getBorderRect().top -= BorderOffset;
                self.getBorderRect().right += BorderOffset;
                self.getBorderRect().bottom += BorderOffset;

                self.getBorderPath().reset();
                self.getBorderPath().addRect(self.getBorderRect(), Path.Direction.CW);

                int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerBorder);
                self.getBorderPaint().setColor(colors[0]);
                self.getBorderPaint().setPathEffect(FirstDashPathEffect);
                canvas.drawPath(self.getBorderPath(), self.getBorderPaint());

                self.getBorderPaint().setColor(colors[1]);
                self.getBorderPaint().setPathEffect(SecondDashPathEffect);
                canvas.drawPath(self.getBorderPath(), self.getBorderPaint());
            }
        }
    }

    /* Delegates */
    /** {@link VDDrawingLayerViewProtocol} */
    @Override
    public void clearDrawing() {
        self.getDrawnSteps().clear();
        self.setCurrentDrawingStep(null);
    }

    @Override
    public VDBrush.Frame appendWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        // 此类图层仅处理 创建图层（当前版本绘制与创建图层为同一step），图层变换 三种操作
        if (drawingStep.getStepType() != VDDrawingStep.StepType.CreateLayer
                && drawingStep.getStepType() != VDDrawingStep.StepType.Transform) {
            return null;
        }

        // 图层尺寸
        VDBrush.Frame frame = null;

        if (drawingStep.getStepType() == VDDrawingStep.StepType.CreateLayer) {
            if (drawingStep.getDrawingState().isVeryBegin()) {
                if (!self.getDrawnSteps().contains(drawingStep)) {
                    self.getDrawnSteps().add(drawingStep);
                }

                self.setCurrentDrawingStep(drawingStep);
            }

            if (drawingStep.getDrawingState().isVeryEnd()) {
                self.setCurrentDrawingStep(null);
                return null;
            }

            // 确定图层尺寸
            frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), drawingStep.getDrawingState().newStateByJoin(VDBrush.DrawingPointerState.FetchFrame));
            drawingStep.getDrawingLayer().setFrame(frame);
        }

        self.updateFrame(drawingStep);

        return frame;
    }

    @Override
    public void refreshWithDrawnSteps(@NonNull List<VDDrawingStep> drawnSteps) {
        self.setDrawnSteps(drawnSteps);
        for (VDDrawingStep step : drawnSteps) {
            self.updateFrame(step);
        }
    }

    @Override
    public int getLayerHierarchy() {
        if (self.getDrawnSteps().size() > 0) {
            return self.getDrawnSteps().get(0).getDrawingLayer().getHierarchy();
        }
        return 0;
    }

    /** {@link VDDrawingLayerViewProtocol#setHandling(boolean)} {@link VDDrawingLayerViewProtocol#isHandling()} */
    private boolean handling;

    @Override
    public boolean isHandling() {
        if (!self.canHandle()) {
            return false;
        }
        return handling;
    }

    @Override
    public void setHandling(boolean handling) {
        if (!self.canHandle()) {
            return;
        }
        self.handling = handling;
        self.invalidate();
    }

    /** {@link VDDrawingLayerViewProtocol#setCanHandle(boolean)} {@link VDDrawingLayerViewProtocol#canHandle()} */
    private boolean canHandle = true;

    @Override
    public boolean canHandle() {
        return self.canHandle;
    }

    @Override
    public void setCanHandle(boolean canHandle) {
        self.canHandle = canHandle;
    }
    /** {@link VDDrawingLayerViewProtocol} */

    /* Private Methods */
    /**
     * 初始化
     */
    private void init() {
        self.setFocusable(true);
        self.setFocusableInTouchMode(true);
    }

    /**
     * 根据step更新图层状态
     * @param drawingStep 更新用step
     */
    private void updateFrame(VDDrawingStep drawingStep) {
        // 更新位置和大小
        if (drawingStep.getDrawingLayer().getFrame() != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) self.getLayoutParams();

            layoutParams.width = (int) Math.floor(drawingStep.getDrawingLayer().getWidth());
            layoutParams.height = (int) Math.floor(drawingStep.getDrawingLayer().getHeight());
            layoutParams.leftMargin = (int) Math.floor(drawingStep.getDrawingLayer().getLeft());
            layoutParams.topMargin = (int) Math.floor(drawingStep.getDrawingLayer().getTop());
            layoutParams.rightMargin = -Integer.MAX_VALUE;
            layoutParams.bottomMargin = -Integer.MAX_VALUE;

            self.setLayoutParams(layoutParams);
        }

        // 更新缩放
        if (drawingStep.getDrawingLayer().getScale() != VDDrawingLayer.UnsetValue) {
            self.setScaleX(drawingStep.getDrawingLayer().getScale());
            self.setScaleY(drawingStep.getDrawingLayer().getScale());
        }

        // 更新旋转
        if (drawingStep.getDrawingLayer().getRotation() != VDDrawingLayer.UnsetValue) {
            self.setRotation(drawingStep.getDrawingLayer().getRotation());
        }

        self.invalidate();
    }
}