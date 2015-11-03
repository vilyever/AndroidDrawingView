package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vilyever.drawingview.brush.VDBrush;
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

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    /* #Constructors */
    public VDDrawingLayerImageView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < self.drawingSteps.size(); i++) {
            VDDrawingStep step = self.drawingSteps.get(i);
            step.getBrush().drawPath(canvas, step.getDrawingPath(), VDBrush.DrawingPointerState.ForceFinish);
        }

        if (self.isSelected()) {
            VDDrawingLayerViewBorderDrawer.drawImageLayerBorder(self, canvas);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) params;
        layoutParams.leftMargin -= DefaultPadding;
        layoutParams.topMargin -= DefaultPadding;
        layoutParams.width += DefaultPadding * 2;
        layoutParams.height += DefaultPadding * 2;
        super.setLayoutParams(params);
    }

    /* #Accessors */

    /* #Delegates */
    // VDDrawingLayerViewDelegate
    @Override
    public void clearDrawing() {
    }

    @Override
    public void updateWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        self.drawingSteps.add(drawingStep);

        if (self.drawingSteps.size() == 1) { // 图层第一笔确定图层大小
            RectF frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), VDBrush.DrawingPointerState.CalibrateToOrigin);
            drawingStep.getDrawingLayer().setFrame(frame);
        }

        self.setLayoutParams(drawingStep.getDrawingLayer().getLayoutParams());
        self.setScaleX(drawingStep.getDrawingLayer().getScale());
        self.setScaleY(drawingStep.getDrawingLayer().getScale());
        self.setRotation(drawingStep.getDrawingLayer().getRotation());

        self.invalidate();
    }

    @Override
    public void updateWithDrawingSteps(@NonNull List<VDDrawingStep> drawingSteps) {
        for (VDDrawingStep step : drawingSteps) {
            self.updateWithDrawingStep(step);
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
        self.invalidate();
    }

    /* #Private Methods */
    private void init(Context context) {
        self.setBackground(null);
        self.setFocusable(true);
        self.setPadding(DefaultPadding, DefaultPadding, DefaultPadding, DefaultPadding);
    }
    
    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}