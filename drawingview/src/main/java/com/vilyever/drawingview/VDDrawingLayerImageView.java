package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.unitconversion.VDDimenConversion;

/**
 * VDDrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingLayerImageView extends ImageView {
    private final VDDrawingLayerImageView self = this;

    public final static int DefaultPadding = VDDimenConversion.dpToPixel(16);

    /* #Constructors */
    public VDDrawingLayerImageView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (self.isSelected()) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            Rect rect = canvas.getClipBounds();
            RectF outline = new RectF(-1, -1, rect.right + 1, rect.bottom + 1);
            Path path = new Path();
            path.addRect(outline, Path.Direction.CW);

            int[] colors = VDContextHolder.getContext().getResources().getIntArray(R.array.DrawingLayerImageBorder);
            paint.setColor(colors[0]);
            paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 1));
            canvas.drawPath(path, paint);

            paint.setColor(colors[1]);
            paint.setPathEffect(new DashPathEffect(new float[]{0, 10, 10, 0}, 1));
            canvas.drawPath(path, paint);
        }
    }

    /* #Accessors */
     
    /* #Delegates */     
     
    /* #Private Methods */
    private void init(Context context) {
        self.setFocusable(true);
    }
    
    /* #Public Methods */
    public void setLayoutParamsWithDefaultPadding(RelativeLayout.LayoutParams params) {
        params.leftMargin -= DefaultPadding;
        params.topMargin -= DefaultPadding;
        params.width += DefaultPadding * 2;
        params.height += DefaultPadding * 2;
        self.setLayoutParams(params);
        self.setPadding(DefaultPadding, DefaultPadding, DefaultPadding, DefaultPadding);
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}