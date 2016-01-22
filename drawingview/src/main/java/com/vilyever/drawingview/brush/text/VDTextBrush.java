package com.vilyever.drawingview.brush.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.model.VDDrawingPath;
import com.vilyever.drawingview.model.VDDrawingPoint;
import com.vilyever.unitconversion.VDDimenConversion;

/**
 * VDTextBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/27.
 * Feature:
 */
public class VDTextBrush extends VDBrush {
    final VDTextBrush self = this;

    public static final int DefaultTextLayerPadding = VDDimenConversion.dpToPixel(8);

    protected float size;
    protected int color;
    protected int typefaceStyle; /** {@link Typeface#NORMAL} {@link Typeface#BOLD} {@link Typeface#ITALIC} {@link Typeface#BOLD_ITALIC} or any created Typeface */

    protected TextPaint textPaint;

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
        return size * self.getDrawingRatio();
    }

    public <T extends VDTextBrush> T setSize(float size) {
        this.size = size;
        self.updateTextPaint();
        return (T) self;
    }

    public int getColor() {
        return color;
    }

    public <T extends VDTextBrush> T setColor(int color) {
        this.color = color;
        self.updateTextPaint();
        return (T) self;
    }

    public int getTypefaceStyle() {
        return typefaceStyle;
    }

    public <T extends VDTextBrush> T setTypefaceStyle(int typefaceStyle) {
        this.typefaceStyle = typefaceStyle;
        self.updateTextPaint();
        return (T) self;
    }

    public TextPaint getTextPaint() {
        if (self.textPaint == null) {
            self.textPaint = new TextPaint();
            self.textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

            self.updateTextPaint();
        }
        return textPaint;
    }

    /* #Delegates */

    /* #Private Methods */
    protected void updateTextPaint() {
        self.getTextPaint().setTextSize(self.getSize());
        self.getTextPaint().setColor(self.getColor());
        self.getTextPaint().setTypeface(Typeface.create((String) null, self.getTypefaceStyle()));
    }

    /* #Public Methods */
    /**
     * drawing text layer border
     * @return
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

    public static VDTextBrush defaultBrush() {
        return new VDTextBrush(14, Color.BLACK);
    }
}