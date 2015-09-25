package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * VDDrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingLayerImageView extends ImageView {
    private final VDDrawingLayerImageView self = this;

    
    /* #Constructors */
    public VDDrawingLayerImageView(Context context) {
        super(context);
    }

    public VDDrawingLayerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VDDrawingLayerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    /* #Overrides */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (self.isSelected()) {
            Paint paint = new Paint();
            paint.setARGB(200, 192, 192, 192);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setPathEffect(new DashPathEffect(new float[]{2, 4, 8, 16}, 1));
            Rect rect = canvas.getClipBounds();
            Rect outline = new Rect(1, 1, rect.right - 1, rect.bottom - 1);
            canvas.drawRect(outline, paint);
        }
    }

    /* #Accessors */
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}