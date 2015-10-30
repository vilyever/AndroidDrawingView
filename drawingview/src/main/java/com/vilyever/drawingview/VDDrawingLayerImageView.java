package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
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

    private RectF outlineRect = new RectF();
    private Path outlinePath = new Path();
    private Paint outlinePaint = new Paint();

    private DashPathEffect firstDashPathEffect = new DashPathEffect(new float[]{10, 10}, 1);
    private DashPathEffect secondDashPathEffect = new DashPathEffect(new float[]{0, 10, 10, 0}, 1);

    /* #Constructors */
    public VDDrawingLayerImageView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */
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
            self.outlinePath.addRect(self.outlineRect, Path.Direction.CW);

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
     
    /* #Delegates */     
     
    /* #Private Methods */
    private void init(Context context) {
        self.setFocusable(true);

        self.outlinePaint.setStyle(Paint.Style.STROKE);
        self.outlinePaint.setStrokeWidth(2);
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