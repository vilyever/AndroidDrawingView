package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.brush.VDTextBrush;

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

    private List<VDDrawingStep> drawingSteps = new ArrayList<>();

    private boolean editing;
    private boolean firstEditing;

    private String unchangedText;

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
        if (self.isSelected()) {
            VDDrawingLayerViewBorderDrawer.drawTextLayerBorder(self, canvas);
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
    public void updateWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        self.drawingSteps.add(drawingStep);

        if (self.drawingSteps.size() == 1) { // 图层第一笔确定图层大小，字体属性
            RectF frame = drawingStep.getBrush().drawPath(null, drawingStep.getDrawingPath(), VDBrush.DrawingPointerState.FetchFrame);
            drawingStep.getDrawingLayer().setFrame(frame);

            VDTextBrush textBrush = drawingStep.getBrush();
            self.setTextSize(textBrush.getSize());
            self.setTextColor(textBrush.getColor());
            self.setTypeface(null, textBrush.getTypefaceStyle());
        }

        self.setText(drawingStep.getDrawingLayer().getText());
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
        self.setPadding(8, 8, 8, 8);
//        self.setGravity(Gravity.LEFT | Gravity.TOP);
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