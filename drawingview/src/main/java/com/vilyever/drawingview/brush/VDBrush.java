package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.VDDrawingPath;
import com.vilyever.jsonmodel.VDJson;
import com.vilyever.jsonmodel.VDModel;

/**
 * VDBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/27.
 * Feature:
 */
public abstract class VDBrush extends VDModel {
    final VDBrush self = this;

    public static final RectF UnfinishFrame = new RectF(-1, -1, -1, -1);
    
    /* #Constructors */    
    
    /* #Overrides */    
    
    /* #Accessors */     
     
    /* #Delegates */     
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public static <T extends VDBrush> T copy(@NonNull VDBrush brush) {
        return (T) new VDJson<>(brush.getClass()).modelFromJson(brush.toJson());
    }

    /**
     *
     * @param canvas the canvas in drawing
     * @param drawingPath the path will draw
     * @param state the finger pointer touching event state
     * @return the frame of the path to draw, null means this path has not enough points to finish a single draw with such brush
     */
    public abstract RectF drawPath(@NonNull Canvas canvas, @NonNull VDDrawingPath drawingPath, DrawingPointerState state);

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum DrawingPointerState {
        Begin, Drawing, End,
        ForceFinish, // 强制完成当前作图
        FetchFrame, // 不做图，以ForceFinish状态获取当前frame
        CalibrateToOrigin; // 所作的图画应从左上角开始显示，因为作图范围超过画布截取出来的图层就会有缺失, 此时以ForceFinish状态作图

        public boolean shouldEnd() {
            switch (this) {
                case Begin:
                case Drawing:
                    return false;
                case End:
                case ForceFinish:
                case FetchFrame:
                case CalibrateToOrigin:
                    return true;
            }

            return false;
        }

        public boolean shouldForceFinish() {
            switch (this) {
                case Begin:
                case Drawing:
                case End:
                    return false;
                case ForceFinish:
                case FetchFrame:
                case CalibrateToOrigin:
                    return true;
            }

            return false;
        }
    }
}