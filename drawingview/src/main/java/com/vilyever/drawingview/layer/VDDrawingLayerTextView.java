package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.R;
import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.text.VDTextBrush;
import com.vilyever.drawingview.model.VDDrawingLayer;
import com.vilyever.drawingview.model.VDDrawingStep;
import com.vilyever.unitconversion.VDDimenConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerTextView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/28.
 * Feature:
 */
public class VDDrawingLayerTextView extends EditText implements VDDrawingLayerViewProtocol {
    final VDDrawingLayerTextView self = this;

    public final static int DefaultPadding = VDDimenConversion.dpToPixel(1);

    private static DashPathEffect FirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect SecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    private List<VDDrawingStep> drawnSteps = new ArrayList<>();

    private boolean editing;
    private boolean firstEditing;

    private String unchangedText;

    private boolean handling;

    /* #Constructors */
    public VDDrawingLayerTextView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (self.isEditing()) {
            super.onTouchEvent(event);
            return true;
        }

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (self.handling) {
            RectF borderRect = new RectF(self.getLeft(), self.getTop(), self.getRight(), self.getBottom());
            int offset = canvas.getClipBounds().bottom - canvas.getHeight();
            borderRect.offsetTo(0, offset);
            borderRect.left -= DefaultPadding;
            borderRect.top -= DefaultPadding;
            borderRect.right += DefaultPadding;
            borderRect.bottom += DefaultPadding;

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            Path path = new Path();
            path.addRoundRect(borderRect, 16, 16, Path.Direction.CW);

            int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerTextBorder);
            paint.setColor(colors[0]);
            paint.setPathEffect(FirstDashPathEffect);
            canvas.drawPath(path, paint);

            paint.setColor(colors[1]);
            paint.setPathEffect(SecondDashPathEffect);
            canvas.drawPath(path, paint);
        }
    }
    
    /* #Accessors */
    public boolean isEditing() {
        return editing;
    }

    public boolean isFirstEditing() {
        return firstEditing;
    }

    public boolean isChanged() {
        return !self.getText().toString().equals(self.unchangedText);
    }

    /* #Delegates */
    // VDDrawingLayerViewDelegate
    @Override
    public void clearDrawing() {
    }

    @Override
    public RectF appendWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        if (drawingStep.getStepType() != VDDrawingStep.StepType.CreateLayer
                && drawingStep.getStepType() != VDDrawingStep.StepType.Text
                && drawingStep.getStepType() != VDDrawingStep.StepType.Transform) {
            return null;
        }

        if (drawingStep.getDrawingState().isVeryBegin()) {
            if (!self.drawnSteps.contains(drawingStep)) {
                self.drawnSteps.add(drawingStep);
            }
        }

        RectF frame = null;
        if (drawingStep.getStepType() == VDDrawingStep.StepType.CreateLayer) { // 图层第一笔确定图层大小，字体属性
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

    /* #Private Methods */
    private void init(Context context) {
        self.setPadding(8, 8, 8, 8);
        self.setBackground(null);
//        self.setGravity(Gravity.LEFT | Gravity.TOP);
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

        if (drawingStep.getBrush() != null) {
            VDTextBrush textBrush = drawingStep.getBrush();
            self.setTextSize(textBrush.getSize());
            self.setTextColor(textBrush.getColor());
            self.setTypeface(null, textBrush.getTypefaceStyle());
        }

        if (drawingStep.getDrawingLayer().getText() != null) {
            self.setText(drawingStep.getDrawingLayer().getText());
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
    public void beginEdit(boolean firstEditing) {
        if (self.isEditing()) {
            return;
        }
        self.editing = true;
        self.firstEditing = firstEditing;

        self.unchangedText = self.getText().toString();

        self.requestFocus();
        InputMethodManager imm = (InputMethodManager) self.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(self, InputMethodManager.SHOW_IMPLICIT);
    }

    public void endEdit() {
        if (!self.isEditing()) {
            return;
        }
        self.editing = false;

        self.clearFocus();
        InputMethodManager imm = (InputMethodManager) self.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(self.getWindowToken(), 0);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}