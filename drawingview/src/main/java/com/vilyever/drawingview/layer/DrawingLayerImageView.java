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

import com.vilyever.contextholder.ContextHolder;
import com.vilyever.drawingview.R;
import com.vilyever.drawingview.brush.Brush;
import com.vilyever.drawingview.model.DrawingLayer;
import com.vilyever.drawingview.model.DrawingStep;
import com.vilyever.unitconversion.DimenConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * DrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 * image图层view，同步绘制，可任意对图层进行变换
 */
public class DrawingLayerImageView extends ImageView implements DrawingLayerViewProtocol {
    private final DrawingLayerImageView self = this;

    /**
     * 边框虚线向外扩张1dp，以免遮挡图形
     * 父view理应取消{@link android.view.ViewGroup#setClipChildren(boolean)}
     */
    public final static int BorderOffset = DimenConverter.dpToPixel(1);
    public final static int BorderWidth = DimenConverter.dpToPixel(2);
    public final static int BorderShadowRadius = DimenConverter.dpToPixel(2);

    /**
     * 边框虚线间距
     */
    private static DashPathEffect FirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect SecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    /* Constructors */
    public DrawingLayerImageView(Context context, int hierarchy) {
        super(context);
        init();
        setLayerHierarchy(hierarchy);
    }

    /* Properties */
    /**
     * 当前绘制的所有step
     */
    private List<DrawingStep> drawnSteps;
    private DrawingLayerImageView setDrawnSteps(List<DrawingStep> drawnSteps) {
        this.drawnSteps = drawnSteps;
        return this;
    }
    public List<DrawingStep> getDrawnSteps() {
        if (this.drawnSteps == null) {
            this.drawnSteps = new ArrayList<>();
        }
        return this.drawnSteps;
    }

    /**
     * 边线绘制矩形
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private RectF borderRect;
    public RectF getBorderRect() {
        if (this.borderRect == null) {
            this.borderRect = new RectF();
        }
        return this.borderRect;
    }

    /**
     * 边线绘制paint
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Paint borderPaint;
    public Paint getBorderPaint() {
        if (this.borderPaint == null) {
            this.borderPaint = new Paint();
            this.borderPaint.setAntiAlias(true);
            this.borderPaint.setDither(true);
            this.borderPaint.setStyle(Paint.Style.STROKE);
            this.borderPaint.setStrokeWidth(BorderWidth);
            this.borderPaint.setShadowLayer(BorderShadowRadius, 0, 0, Color.DKGRAY);
        }
        return this.borderPaint;
    }

    /**
     * 边线绘制path
     * 因onDraw调用频繁，不宜在onDraw内new新对象
     */
    private Path borderPath;
    public Path getBorderPath() {
        if (this.borderPath == null) {
            this.borderPath = new Path();
        }
        return this.borderPath;
    }

    /* Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 绘制图形
         */
        if (getDrawnSteps().size() > 0) {
            // 默认第一step位绘制step，即创建图层的step同时进行绘画
            // 之后的步骤都只是对图层进行变换
            DrawingStep step = getDrawnSteps().get(0);

            if (step.getStepType() != DrawingStep.StepType.CreateLayer) {
                return;
            }

            // 绘制图形，平移图形到图层左上角
            step.getBrush().drawPath(canvas, step.getDrawingPath(), step.getDrawingState().newStateByJoin(Brush.DrawingPointerState.CalibrateToOrigin));

            // 图层在被操作时，绘制边框
            if (isHandling()) {
                getBorderRect().set(getLeft(), getTop(), getRight(), getBottom());
                getBorderRect().offsetTo(0, 0);
                getBorderRect().left -= BorderOffset;
                getBorderRect().top -= BorderOffset;
                getBorderRect().right += BorderOffset;
                getBorderRect().bottom += BorderOffset;

                getBorderPath().reset();
                getBorderPath().addRect(getBorderRect(), Path.Direction.CW);

                int[] colors = ContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerBorder);
                getBorderPaint().setColor(colors[0]);
                getBorderPaint().setPathEffect(FirstDashPathEffect);
                canvas.drawPath(getBorderPath(), getBorderPaint());

                getBorderPaint().setColor(colors[1]);
                getBorderPaint().setPathEffect(SecondDashPathEffect);
                canvas.drawPath(getBorderPath(), getBorderPaint());
            }
        }
    }

    /* Delegates */
    /** {@link DrawingLayerViewProtocol} */
    @Override
    public void clearDrawing() {
        getDrawnSteps().clear();
        invalidate();
    }

    @Override
    public Brush.Frame appendWithDrawingStep(@NonNull DrawingStep drawingStep) {
        // 此类图层仅处理 创建图层（当前版本绘制与创建图层为同一step），图层变换 三种操作
        if (drawingStep.getStepType() != DrawingStep.StepType.CreateLayer
                && drawingStep.getStepType() != DrawingStep.StepType.Transform) {
            return null;
        }

        // 图层尺寸
        Brush.Frame frame = null;

        if (drawingStep.getStepType() == DrawingStep.StepType.CreateLayer) {
            if (!getDrawnSteps().contains(drawingStep)) {
                if (getDrawnSteps().size() > 0) {
                    if (getDrawnSteps().get(getDrawnSteps().size() - 1).getStep() == drawingStep.getStep()) {
                        getDrawnSteps().remove(getDrawnSteps().size() - 1);
                    }
                }

                getDrawnSteps().add(drawingStep);
            }

            if (drawingStep.getDrawingState().isVeryEnd()) {
                return null;
            }

            // 确定图层尺寸
            frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), drawingStep.getDrawingState().newStateByJoin(Brush.DrawingPointerState.FetchFrame));
            drawingStep.getDrawingLayer().setFrame(frame);
        }

        updateFrame(drawingStep);

        return frame;
    }

    @Override
    public void appendWithSteps(@NonNull List<DrawingStep> steps) {
        getDrawnSteps().addAll(steps);
        for (DrawingStep step : steps) {
            updateFrame(step);
        }
    }

    @Override
    public void refreshWithDrawnSteps(@NonNull List<DrawingStep> drawnSteps) {
        getDrawnSteps().clear();
        appendWithSteps(drawnSteps);
    }


    /** {@link DrawingLayerViewProtocol#getLayerHierarchy()} */
    private int hierarchy;

    @Override
    public int getLayerHierarchy() {
        return hierarchy;
    }

    @Override
    public void setLayerHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    /** {@link DrawingLayerViewProtocol#setHandling(boolean)} {@link DrawingLayerViewProtocol#isHandling()} */
    private boolean handling;

    @Override
    public boolean isHandling() {
        if (!canHandle()) {
            return false;
        }
        return this.handling;
    }

    @Override
    public void setHandling(boolean handling) {
        if (!canHandle()) {
            return;
        }
        this.handling = handling;
        invalidate();
    }

    /** {@link DrawingLayerViewProtocol#setCanHandle(boolean)} {@link DrawingLayerViewProtocol#canHandle()} */
    private boolean canHandle = true;

    @Override
    public boolean canHandle() {
        return this.canHandle;
    }

    @Override
    public void setCanHandle(boolean canHandle) {
        this.canHandle = canHandle;
    }
    /** {@link DrawingLayerViewProtocol} */

    /* Private Methods */
    /**
     * 初始化
     */
    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    /**
     * 根据step更新图层状态
     * @param drawingStep 更新用step
     */
    private void updateFrame(DrawingStep drawingStep) {
        // 更新位置和大小
        if (drawingStep.getDrawingLayer().getFrame() != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();

            layoutParams.width = (int) Math.floor(drawingStep.getDrawingLayer().getWidth());
            layoutParams.height = (int) Math.floor(drawingStep.getDrawingLayer().getHeight());
            layoutParams.leftMargin = (int) Math.floor(drawingStep.getDrawingLayer().getLeft());
            layoutParams.topMargin = (int) Math.floor(drawingStep.getDrawingLayer().getTop());
            layoutParams.rightMargin = -Integer.MAX_VALUE;
            layoutParams.bottomMargin = -Integer.MAX_VALUE;

            setLayoutParams(layoutParams);
        }

        // 更新缩放
        if (drawingStep.getDrawingLayer().getScale() != DrawingLayer.UnsetValue) {
            setScaleX(drawingStep.getDrawingLayer().getScale());
            setScaleY(drawingStep.getDrawingLayer().getScale());
        }

        // 更新旋转
        if (drawingStep.getDrawingLayer().getRotation() != DrawingLayer.UnsetValue) {
            setRotation(drawingStep.getDrawingLayer().getRotation());
        }

        invalidate();
    }
}