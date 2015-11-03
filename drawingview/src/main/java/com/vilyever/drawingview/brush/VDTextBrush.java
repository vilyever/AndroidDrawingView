package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.drawingview.VDDrawingPoint;
import com.vilyever.unitconversion.VDDimenConversion;

/**
 * VDTextBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/27.
 * Feature:
 */
public class VDTextBrush extends VDBrush {
    final VDTextBrush self = this;

    private static final int BorderMargin = VDDimenConversion.dpToPixel(8);;

    protected float size;
    protected int color;
    protected int typefaceStyle; // Typeface.NORMAL || Typeface.BOLD || Typeface.ITALIC || Typeface.BOLD_ITALIC or any created Typeface

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

    /* #Overrides */

    /* #Accessors */
    public float getSize() {
        return size;
    }

    public <T extends VDTextBrush> T setSize(float size) {
        this.size = size;
        return (T) self;
    }

    public int getColor() {
        return color;
    }

    public <T extends VDTextBrush> T setColor(int color) {
        this.color = color;
        return (T) self;
    }

    public int getTypefaceStyle() {
        return typefaceStyle;
    }

    public <T extends VDTextBrush> T setTypefaceStyle(int typefaceStyle) {
        this.typefaceStyle = typefaceStyle;
        return (T) self;
    }

    /* #Delegates */

    /* #Private Methods */

    /* #Public Methods */
    /**
     * drawing text layer border
     * @return
     */
    @Override
    public RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state) {
        if (drawingPath.getPoints().size() > 0) {
            VDDrawingPoint beginPoint = drawingPath.getPoints().get(0);
            VDDrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            RectF drawingRect = new RectF();
            drawingRect.left = Math.min(beginPoint.x, lastPoint.x);
            drawingRect.top = Math.min(beginPoint.y, lastPoint.y);
            drawingRect.right = Math.max(beginPoint.x, lastPoint.x);
            drawingRect.bottom = Math.max(beginPoint.y, lastPoint.y);

            drawingRect.left -= BorderMargin;
            drawingRect.top -= BorderMargin * 3;

            TextPaint textPaint = new TextPaint();
            textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(self.getSize());
            textPaint.setColor(self.getColor());
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

            Rect result = new Rect();
            textPaint.getTextBounds("█", 0, "█".length(), result);

            float minTextWidth = textPaint.measureText("　") * 20.0f;
            float minTextHeight = fontMetrics.ascent + fontMetrics.descent + fontMetrics.leading;

            minTextWidth = result.width() * 22;
            minTextHeight = result.height();

            drawingRect.right = Math.max(drawingRect.right, drawingRect.left + minTextWidth + BorderMargin);
            drawingRect.bottom = Math.max(drawingRect.bottom, drawingRect.top + minTextHeight + BorderMargin * 4);

            RectF pathFrame = new RectF(drawingRect);
            return pathFrame;
        }

        return null;
    }

    public static VDTextBrush defaultBrush() {
        return new VDTextBrush(20, Color.BLACK);
    }

    /* #Classes */

    /* #Interfaces */

    /* #Annotations @interface */

    /* #Enums */
}