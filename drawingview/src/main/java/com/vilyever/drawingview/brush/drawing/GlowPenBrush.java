package com.vilyever.drawingview.brush.drawing;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.R;
import com.vilyever.drawingview.model.DrawingPath;
import com.vilyever.resource.Resource;

/**
 * GlowPenBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush.drawing>
 * Created by vilyever on 2016/2/25.
 * Feature:
 * 炫光笔
 * 推荐在黑色背景下使用
 */
public class GlowPenBrush extends PenBrush {
    final GlowPenBrush self = this;

    
    /* Constructors */
    public GlowPenBrush() {
    }

    public GlowPenBrush(float size, int color) {
        super(size, color);
    }
    
    
    /* Public Methods */
    public static GlowPenBrush defaultBrush() {
        return new GlowPenBrush(Resource.getDimensionPixelSize(R.dimen.drawingViewBrushDefaultSize), Color.RED);
    }
    
    
    /* Properties */
    private boolean onBlurDraw;
    private <T extends GlowPenBrush> T setOnBlurDraw(boolean onBlurDraw) {
        this.onBlurDraw = onBlurDraw;
        return (T) this;
    }
    public boolean isOnBlurDraw() {
        return this.onBlurDraw;
    }
    
    
    /* Overrides */
    @Override
    protected void updatePaint() {
        super.updatePaint();

        if (isOnBlurDraw()) {
            getPaint().setStrokeWidth(getSize() * 2.0f);
            getPaint().setMaskFilter(new BlurMaskFilter(getSize(), BlurMaskFilter.Blur.NORMAL));
        }
        else {
            getPaint().setColor(Color.WHITE);
            getPaint().setMaskFilter(null);
        }
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull DrawingPath drawingPath, @NonNull DrawingState state) {
        setOnBlurDraw(true);
        super.drawPath(canvas, drawingPath, state);
        setOnBlurDraw(false);
        return super.drawPath(canvas, drawingPath, state);
    }
     
    /* Delegates */
     
     
    /* Private Methods */
    
}