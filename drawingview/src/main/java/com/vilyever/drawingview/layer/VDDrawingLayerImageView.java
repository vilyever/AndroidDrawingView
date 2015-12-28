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
 */
public class VDDrawingLayerImageView extends ImageView implements VDDrawingLayerViewProtocol {
    private final VDDrawingLayerImageView self = this;

    public final static int DefaultPadding = VDDimenConversion.dpToPixel(1);

    private static DashPathEffect FirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect SecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    private List<VDDrawingStep> drawnSteps = new ArrayList<>();

    private VDDrawingStep currentDrawingStep;

    private boolean handling;

    private boolean canHandle;

    /* #Constructors */
    public VDDrawingLayerImageView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (self.drawnSteps.size() > 0) {
            VDDrawingStep step = self.drawnSteps.get(0);

            RectF frame = step.getBrush().drawPath(canvas, step.getDrawingPath(), step.getDrawingState().newStateByJoin(VDBrush.DrawingPointerState.CalibrateToOrigin));

            if (self.handling && frame != null) {
                RectF borderRect = new RectF(self.getLeft(), self.getTop(), self.getRight(), self.getBottom());
                borderRect.offsetTo(0, 0);
                borderRect.left -= DefaultPadding;
                borderRect.top -= DefaultPadding;
                borderRect.right += DefaultPadding;
                borderRect.bottom += DefaultPadding;

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setDither(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                paint.setShadowLayer(2, 0, 0, Color.DKGRAY);

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
    public List<VDDrawingStep> getDrawnSteps() {
        return drawnSteps;
    }

    public boolean isCanHandle() {
        return canHandle;
    }

    /* #Delegates */
    // VDDrawingLayerViewDelegate
    @Override
    public void clearDrawing() {
        self.drawnSteps.clear();
        self.currentDrawingStep = null;
    }

    @Override
    public VDBrush.Frame appendWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        if (drawingStep.getStepType() != VDDrawingStep.StepType.CreateLayer
                && drawingStep.getStepType() != VDDrawingStep.StepType.Draw
                && drawingStep.getStepType() != VDDrawingStep.StepType.Transform) {
            return null;
        }

        if (drawingStep.getDrawingState().isVeryBegin()) {
            if (!self.drawnSteps.contains(drawingStep)) {
                self.drawnSteps.add(drawingStep);
            }

            self.currentDrawingStep = drawingStep;
        }

        if (drawingStep.getDrawingState().isVeryEnd()) {
            self.currentDrawingStep = null;
            return null;
        }

        VDBrush.Frame frame = null;
        if (drawingStep.getStepType() == VDDrawingStep.StepType.CreateLayer) { // 图层第一笔确定图层大小
            frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), drawingStep.getDrawingState().newStateByJoin(VDBrush.DrawingPointerState.FetchFrame));
            drawingStep.getDrawingLayer().setFrame(frame);
        }

        self.updateFrame(drawingStep);

        return frame;
    }

    @Override
    public void refreshWithDrawnSteps(@NonNull List<VDDrawingStep> drawnSteps) {
        self.drawnSteps = drawnSteps;
        for (VDDrawingStep step : drawnSteps) {
            self.updateFrame(step);
        }
    }

    @Override
    public int getLayerHierarchy() {
        if (self.drawnSteps.size() > 0) {
            return self.drawnSteps.get(0).getDrawingLayer().getHierarchy();
        }
        return 0;
    }

    @Override
    public void setHandling(boolean handling) {
        self.handling = handling;
        self.invalidate();
    }

    @Override
    public boolean canHandle() {
        return self.canHandle;
    }

    @Override
    public void setCanHandle(boolean canHandle) {
        self.canHandle = canHandle;
    }

    /* #Private Methods */
    private void init(Context context) {
        self.setFocusable(true);
        self.setFocusableInTouchMode(true);
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

        if (drawingStep.getDrawingLayer().getScale() != VDDrawingLayer.UnsetValue) {
            self.setScaleX(drawingStep.getDrawingLayer().getScale());
            self.setScaleY(drawingStep.getDrawingLayer().getScale());
        }

        if (drawingStep.getDrawingLayer().getRotation() != VDDrawingLayer.UnsetValue) {
            self.setRotation(drawingStep.getDrawingLayer().getRotation());
        }

        self.invalidate();
    }
    
    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}