package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Canvas;
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
import com.vilyever.drawingview.brush.drawing.VDDrawingBrush;
import com.vilyever.drawingview.model.VDDrawingStep;
import com.vilyever.unitconversion.VDDimenConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingLayerImageView extends ImageView implements VDDrawingLayerViewProtocol {
    private final VDDrawingLayerImageView self = this;

    public final static int DefaultPadding = VDDimenConversion.dpToPixel(16);

    private static DashPathEffect FirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect SecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    private VDDrawingStep currentDrawingStep;
    private VDBrush.DrawingPointerState currentDrawingState;

    /* #Constructors */
    public VDDrawingLayerImageView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (self.drawingSteps.size() > 0) {
            VDDrawingStep step = self.drawingSteps.get(0);

            VDBrush.DrawingPointerState state = self.currentDrawingState.shouldForceFinish() ? VDBrush.DrawingPointerState.ForceCalibrateToOrigin : VDBrush.DrawingPointerState.CalibrateToOrigin;
            RectF frame = step.getBrush().drawPath(canvas, step.getDrawingPath(), state);

            if (self.isSelected() && frame != null) {
                RectF borderRect = new RectF(self.getLeft(), self.getTop(), self.getRight(), self.getBottom());
                borderRect.offsetTo(0, 0);
                borderRect.left -= DefaultPadding;
                borderRect.top -= DefaultPadding;
                borderRect.right += DefaultPadding;
                borderRect.bottom += DefaultPadding;

                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);

                Path path = new Path();
                path.addRect(borderRect, Path.Direction.CW);

                int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerBorder);
                paint.setColor(colors[0]);
                paint.setPathEffect(FirstDashPathEffect);
                canvas.drawPath(path, paint);

                paint.setColor(colors[1]);
                paint.setPathEffect(SecondDashPathEffect);
                canvas.drawPath(path, paint);
            }
        }
    }

    /* #Accessors */
    public List<VDDrawingStep> getDrawingSteps() {
        return drawingSteps;
    }

    /* #Delegates */
    // VDDrawingLayerViewDelegate
    @Override
    public void clearDrawing() {
        self.drawingSteps.clear();
        self.currentDrawingStep = null;
        self.currentDrawingState = null;
    }

    @Override
    public RectF updateWithDrawingStep(@NonNull VDDrawingStep drawingStep, VDDrawingBrush.DrawingPointerState state) {
        if (drawingStep.getStepType() != VDDrawingStep.StepType.Draw
                && drawingStep.getStepType() != VDDrawingStep.StepType.Frame) {
            return null;
        }

        if (!self.drawingSteps.contains(drawingStep)) {
            self.drawingSteps.add(drawingStep);
        }

        if (self.drawingSteps.indexOf(drawingStep) == 0) { // 图层第一笔确定图层大小
            RectF frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinishFetchFrame);
            drawingStep.getDrawingLayer().setFrame(frame);

            self.currentDrawingStep = drawingStep;
            self.currentDrawingState = state;
        }

        self.updateFrame(drawingStep);

        if (self.drawingSteps.indexOf(drawingStep) == 0) {
            VDBrush.DrawingPointerState frameState = VDBrush.DrawingPointerState.FetchFrame;
            if (state.shouldForceFinish()) {
                frameState = VDBrush.DrawingPointerState.ForceFinishFetchFrame;
            }
            RectF frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), frameState);
            drawingStep.getDrawingLayer().setFrame(frame);

            return frame;
        }

        return null;
    }

    @Override
    public void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps) {
        for (VDDrawingStep step : drawingSteps) {
            self.updateWithDrawingStep(step, VDBrush.DrawingPointerState.ForceFinish);
        }
    }

    @Override
    public int getLayerHierarchy() {
        if (self.drawingSteps.size() > 0) {
            return self.drawingSteps.get(0).getDrawingLayer().getHierarchy();
        }
        return 0;
    }

    @Override
    public void setHandling(boolean handling) {
        self.setSelected(handling);
    }

    /* #Private Methods */
    private void init(Context context) {
        self.setFocusable(true);
    }

    private void updateFrame(VDDrawingStep drawingStep) {
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
        self.setScaleX(drawingStep.getDrawingLayer().getScale());
        self.setScaleY(drawingStep.getDrawingLayer().getScale());
        self.setRotation(drawingStep.getDrawingLayer().getRotation());
        self.invalidate();
    }
    
    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}