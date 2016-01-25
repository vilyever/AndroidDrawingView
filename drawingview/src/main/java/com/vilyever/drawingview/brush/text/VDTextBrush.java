package com.vilyever.drawingview.brush.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;
import com.vilyever.unitconversion.VDDimenConversion;

/**
 * VDTextBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/27.
 * Feature:
 * 绘制文本框，实际上文本框由{@link com.vilyever.drawingview.layer.VDDrawingLayerTextView}自行判断大小绘制
 * 此brush仅确定文本框的初始位置
 */
public class VDTextBrush extends VDBrush {
    final VDTextBrush self = this;

    public static final int DefaultTextLayerPadding = VDDimenConversion.dpToPixel(8);

    /* #Constructors */
    public VDTextBrush() {
    }

    public VDTextBrush(float size, int color) {
        this(size, color, Typeface.NORMAL);
    }

    public VDTextBrush(float size, int color, int typefaceStyle) {
        this.size = size;
        this.color = color;
        this.typefaceStyle = typefaceStyle;
    }

    /* Public Methods */
    /**
     * 默认brush
     * @return 默认brush
     */
    public static VDTextBrush defaultBrush() {
        return new VDTextBrush(14, Color.BLACK);
    }

    /* Properties */
    /**
     * 字体大小
     */
    protected float size;
    public float getSize() {
        return size * self.getDrawingRatio();
    }
    public <T extends VDTextBrush> T setSize(float size) {
        this.size = size;
        return (T) self;
    }

    /**
     * 字体颜色
     */
    protected int color;
    public int getColor() {
        return color;
    }
    public <T extends VDTextBrush> T setColor(int color) {
        this.color = color;
        return (T) self;
    }

    /**
     * 字体样式
     */
    protected int typefaceStyle; /** {@link Typeface#NORMAL} {@link Typeface#BOLD} {@link Typeface#ITALIC} {@link Typeface#BOLD_ITALIC} or any created Typeface */
    public int getTypefaceStyle() {
        return typefaceStyle;
    }
    public <T extends VDTextBrush> T setTypefaceStyle(int typefaceStyle) {
        this.typefaceStyle = typefaceStyle;
        return (T) self;
    }

    /* Overrides */
    /**
     * 定位text图层位置
     * @return 图层位置
     */
    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state) {
        if (drawingPath.getPoints().size() > 0) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

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