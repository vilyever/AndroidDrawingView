package com.vilyever.drawingview;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.vilyever.contextholder.VDContextHolder;

/**
 * VDDrawingLayerViewBorderDrawer
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/30.
 * Feature:
 */
public class VDDrawingLayerViewBorderDrawer {
    final VDDrawingLayerViewBorderDrawer self = this;

    private static RectF OutlineRect = new RectF();
    private static Path OutlinePath = new Path();
    private static Paint OutlinePaint = new Paint();

    private static DashPathEffect ImageLayerFirstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private static DashPathEffect ImageLayerSecondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);
    
    /* #Constructors */    
    
    /* #Overrides */    
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static void drawImageLayerBorder(VDDrawingLayerImageView imageView, Canvas canvas) {
        OutlinePaint.setStyle(Paint.Style.STROKE);
        OutlinePaint.setStrokeWidth(2);

        OutlineRect.left = -1;
        OutlineRect.top = -1;
        OutlineRect.right = imageView.getWidth() + 1;
        OutlineRect.bottom = imageView.getHeight() + 1;
        
        int offset = canvas.getClipBounds().bottom - canvas.getHeight();
        OutlineRect.offset(0, offset);

        OutlinePath.reset();
        OutlinePath.addRect(OutlineRect, Path.Direction.CW);

        int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerBorder);
        OutlinePaint.setColor(colors[0]);
        OutlinePaint.setPathEffect(ImageLayerFirstDashPathEffect);
        canvas.drawPath(OutlinePath, OutlinePaint);

        OutlinePaint.setColor(colors[1]);
        OutlinePaint.setPathEffect(ImageLayerSecondDashPathEffect);
        canvas.drawPath(OutlinePath, OutlinePaint);
    }
    
    public static void drawTextLayerBorder(VDDrawingLayerTextView textView, Canvas canvas) {
        OutlinePaint.setStyle(Paint.Style.STROKE);
        OutlinePaint.setStrokeWidth(2);

        OutlineRect.left = -1;
        OutlineRect.top = -1;
        OutlineRect.right = textView.getWidth() + 1;
        OutlineRect.bottom = textView.getHeight() + 1;

        int offset = canvas.getClipBounds().bottom - canvas.getHeight();
        OutlineRect.offset(0, offset);

        OutlinePath.reset();
        OutlinePath.addRoundRect(OutlineRect, 16, 16, Path.Direction.CW);

        int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerBorder);
        OutlinePaint.setColor(colors[0]);
        OutlinePaint.setPathEffect(ImageLayerFirstDashPathEffect);
        canvas.drawPath(OutlinePath, OutlinePaint);

        OutlinePaint.setColor(colors[1]);
        OutlinePaint.setPathEffect(ImageLayerSecondDashPathEffect);
        canvas.drawPath(OutlinePath, OutlinePaint);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}