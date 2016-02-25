package com.vilyever.drawingview.brush.drawing;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;

/**
 * VDGlowPenBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush.drawing>
 * Created by vilyever on 2016/2/25.
 * Feature:
 * 炫光笔
 * 推荐在黑色背景下使用
 */
public class VDGlowPenBrush extends VDPenBrush {
    final VDGlowPenBrush self = this;

    
    /* Constructors */
    public VDGlowPenBrush() {
    }

    public VDGlowPenBrush(float size, int color) {
        super(size, color);
    }
    
    
    /* Public Methods */
    public static VDGlowPenBrush defaultBrush() {
        return new VDGlowPenBrush(5, Color.RED);
    }
    
    
    /* Properties */
    private boolean onBlurDraw;
    private VDGlowPenBrush setOnBlurDraw(boolean onBlurDraw) {
        this.onBlurDraw = onBlurDraw;
        return this;
    }
    public boolean isOnBlurDraw() {
        return onBlurDraw;
    }
    
    
    /* Overrides */
    @Override
    protected void updatePaint() {
        super.updatePaint();

        if (self.isOnBlurDraw()) {
            self.getPaint().setStrokeWidth(self.getSize() * 2.0f);
            self.getPaint().setMaskFilter(new BlurMaskFilter(self.getSize(), BlurMaskFilter.Blur.NORMAL));
        }
        else {
            self.getPaint().setColor(Color.WHITE);
            self.getPaint().setMaskFilter(null);
        }
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        self.setOnBlurDraw(true);
        super.drawPath(canvas, drawingPath, state);
        self.setOnBlurDraw(false);
        return super.drawPath(canvas, drawingPath, state);
    }
     
    /* Delegates */
     
     
    /* Private Methods */
    
}