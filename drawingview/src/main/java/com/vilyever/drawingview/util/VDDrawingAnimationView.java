package com.vilyever.drawingview.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.R;

/**
 * VDDrawingAnimationView
 * AndroidDrawingView <com.vilyever.drawingview.util>
 * Created by vilyever on 2015/12/1.
 * Feature:
 */
public class VDDrawingAnimationView extends View {
    final VDDrawingAnimationView self = this;

    private boolean animated;

    private Drawable animitionDrawable = VDContextHolder.getContext().getResources().getDrawable(R.drawable.animation_drawing_brush);

    private float x = 0;

    private PathMeasure pathMeasure;
    float[] point = new float[2];

    /* #Constructors */
    public VDDrawingAnimationView(Context context) {
        this(context, null);
    }

    public VDDrawingAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VDDrawingAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        self.initial(context);
    }
    
    /* #Overrides */

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            self.pathMeasure = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        if (self.animated) {
//            self.getPathMeasure().getPosTan(pathMeasure.getLength() * (x / 100.0f), self.point, null);
//
//            int centerX = (int) self.point[0];
//            int centerY = (int) self.point[1];
//
//            self.animitionDrawable.setBounds(centerX - self.animitionDrawable.getIntrinsicWidth() / 2,
//                    centerY - self.animitionDrawable.getIntrinsicHeight() / 2,
//                    centerX + self.animitionDrawable.getIntrinsicWidth() / 2,
//                    centerY + self.animitionDrawable.getIntrinsicHeight() / 2);
//            self.animitionDrawable.draw(canvas);
//
//            x = (x + 2) % 100;
//
//            self.invalidate();
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /* #Accessors */
    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
        self.x = 0;
        self.invalidate();
    }

    public PathMeasure getPathMeasure() {
        if (self.pathMeasure == null) {

            int width = self.getWidth() / 12;
            int height = self.getHeight() / 12;

            int left = (self.getWidth() - width) / 2;
            int top = (self.getHeight() - height) / 2;

            Path path = new Path();

            int x = left;
            int y = top + height / 2;

            path.moveTo(x, y);
            path.lineTo(left + width / 2, top);
            path.lineTo(left + width / 6, top + height);
            path.lineTo(left + width, top + height / 6);

            self.pathMeasure = new PathMeasure(path, false);
        }
        return pathMeasure;
    }

    /* #Delegates */
     
    /* #Private Methods */
    private void initial(Context context) {
        self.setBackground(null);
    }

    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}