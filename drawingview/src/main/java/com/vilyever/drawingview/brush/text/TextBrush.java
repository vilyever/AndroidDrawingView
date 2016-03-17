package com.vilyever.drawingview.brush.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.R;
import com.vilyever.drawingview.brush.Brush;
import com.vilyever.drawingview.model.DrawingPath;
import com.vilyever.drawingview.model.DrawingPoint;
import com.vilyever.resource.Resource;

/**
 * TextBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/27.
 * Feature:
 * 绘制文本框，实际上文本框由{@link com.vilyever.drawingview.layer.DrawingLayerTextView}自行判断大小绘制
 * 此brush仅确定文本框的初始位置
 */
public class TextBrush extends Brush {
    final TextBrush self = this;

    /* #Constructors */
    public TextBrush() {
    }

    public TextBrush(float size, int color) {
        this(size, color, Typeface.NORMAL);
    }

    public TextBrush(float size, int color, int typefaceStyle) {
        this.size = size;
        this.color = color;
        this.typefaceStyle = typefaceStyle;
    }

    /* Public Methods */
    /**
     * 默认brush
     * @return 默认brush
     */
    public static TextBrush defaultBrush() {
        return new TextBrush(Resource.getDimensionPixelSize(R.dimen.drawingViewTextBrushDefaultSize), Color.BLACK);
    }

    /* Properties */
    /**
     * 字体大小
     */
    protected float size;
    public float getSize() {
        return this.size * getDrawingRatio();
    }
    public <T extends TextBrush> T setSize(float size) {
        this.size = size;
        return (T) this;
    }

    /**
     * 字体颜色
     */
    protected int color;
    public int getColor() {
        return this.color;
    }
    public <T extends TextBrush> T setColor(int color) {
        this.color = color;
        return (T) this;
    }

    /**
     * 字体样式
     */
    protected int typefaceStyle; /** {@link Typeface#NORMAL} {@link Typeface#BOLD} {@link Typeface#ITALIC} {@link Typeface#BOLD_ITALIC} or any created Typeface */
    public int getTypefaceStyle() {
        return this.typefaceStyle;
    }
    public <T extends TextBrush> T setTypefaceStyle(int typefaceStyle) {
        this.typefaceStyle = typefaceStyle;
        return (T) this;
    }

    /* Overrides */
    /**
     * 定位text图层位置
     * @return 图层位置
     */
    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull DrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 0) {
            DrawingPoint beginPoint = drawingPath.getPoints().get(0);
            DrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();

            drawingRect.left = Math.max(lastPoint.getX(), 0);
            drawingRect.top = Math.max(lastPoint.getY(), 0);

            drawingRect.right = drawingRect.left;
            drawingRect.bottom = drawingRect.top;

            Frame pathFrame = new Frame(drawingRect);
            return pathFrame;
        }

        return Frame.EmptyFrame();
    }
}