package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;

/**
 * VDDrawingLayerTextView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/28.
 * Feature:
 */
public class VDDrawingLayerTextView extends EditText {
    final VDDrawingLayerTextView self = this;

    private RectF outlineRect = new RectF();
    private Path outlinePath = new Path();
    private Paint outlinePaint = new Paint();

    private DashPathEffect firstDashPathEffect = new DashPathEffect(new float[]{20, 20}, 1);
    private DashPathEffect secondDashPathEffect = new DashPathEffect(new float[]{0, 20, 20, 0}, 1);

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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            self.outlineRect.left = -1;
            self.outlineRect.top = -1;
            self.outlineRect.right = right - left + 1;
            self.outlineRect.bottom = bottom - top + 1;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (self.isSelected()) {
            int offset = canvas.getClipBounds().bottom - canvas.getHeight();
            self.outlineRect.offset(0, offset);

            self.outlinePath.reset();
            self.outlinePath.addRoundRect(self.outlineRect, 16, 16, Path.Direction.CW);

            int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerBorder);
            self.outlinePaint.setColor(colors[0]);
            self.outlinePaint.setPathEffect(self.firstDashPathEffect);
            canvas.drawPath(self.outlinePath, self.outlinePaint);

            self.outlinePaint.setColor(colors[1]);
            self.outlinePaint.setPathEffect(self.secondDashPathEffect);
            canvas.drawPath(self.outlinePath, self.outlinePaint);

            self.outlineRect.offsetTo(0, 0);
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
     
    /* #Private Methods */
    private void init(Context context) {
        self.setFocusable(true);
        self.setBackground(null);
        self.setPadding(8, 8, 8, 8);
//        self.setGravity(Gravity.LEFT | Gravity.TOP);

        self.outlinePaint.setStyle(Paint.Style.STROKE);
        self.outlinePaint.setStrokeWidth(2);
    }
    
    /* #Public Methods */
    public void setLayoutParamsWithDefaultPadding(RelativeLayout.LayoutParams params) {
        self.setLayoutParams(params);
    }

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