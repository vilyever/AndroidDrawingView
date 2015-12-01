package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.model.VDDrawingPath;
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

    // 在判断一笔是否结束时，未能满足结束条件时返回的Frame
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
     * @param state drawing state
     * @return 绘制图形所处的Frame，不同state可能返回不同坐标，若返回null表示path不足以作图，若返回UnfinishFrame表示当前path不能完成作图，需要进一步触摸绘制
     */
    public abstract RectF drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state);

    /**
     *
     * @return 同一笔绘图时是否在path变更时消除先前作图
     */
    public boolean shouldDrawFromBegin() {
        return true;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum DrawingPointerState {
        TouchDown, TouchMoving, TouchUp, // 手指状态，用于作图，因支持多次触摸完成一笔，down和up可能出现多次
        FetchFrame, // 忽略一笔完成所需条件，由笔刷自行补完未完成部分
        CalibrateToOrigin, // 所作的图画应从画布左上角开始绘制
        ForceFinish, // 不做图，获取当前frame
        VeryBegin, VeryEnd; // 一笔起始/结束，通常用于标记

        public int state() {
            switch (this) {
                case TouchDown:
                    return 0x00000001;
                case TouchMoving:
                    return 0x00000010;
                case TouchUp:
                    return 0x00000100;
                case FetchFrame:
                    return 0x00001000;
                case CalibrateToOrigin:
                    return 0x00010000;
                case ForceFinish:
                    return 0x00100000;
                case VeryBegin:
                    return 0x01000000;
                case VeryEnd:
                    return 0x10000000;
            }
            return 0;
        }
    }

    public static final class DrawingState extends VDModel {
        private int pointerState;
        public DrawingState(DrawingPointerState ... states) {
            for (DrawingPointerState state : states) {
                this.pointerState |= state.state();
            }
        }

        public DrawingState(int pointerState) {
            this.pointerState = pointerState;
        }

        public DrawingState join(DrawingPointerState state) {
            this.pointerState |= state.state();
            return this;
        }

        public DrawingState newStateByJoin(DrawingPointerState state) {
            DrawingState drawingState = new DrawingState(this.pointerState);
            drawingState.pointerState |= state.state();
            return drawingState;
        }

        public DrawingState separate(DrawingPointerState state) {
            this.pointerState &= ~state.state();
            return this;
        }

        public DrawingState newStateBySeparate(DrawingPointerState state) {
            DrawingState drawingState = new DrawingState(this.pointerState);
            drawingState.pointerState &= ~state.state();
            return drawingState;
        }

        public boolean isTouchDown() {
            return (this.pointerState & DrawingPointerState.TouchDown.state()) == DrawingPointerState.TouchDown.state();
        }
        public boolean isTouchMoving() {
            return (this.pointerState & DrawingPointerState.TouchMoving.state()) == DrawingPointerState.TouchMoving.state();
        }
        public boolean isTouchUp() {
            return (this.pointerState & DrawingPointerState.TouchUp.state()) == DrawingPointerState.TouchUp.state();
        }
        public boolean isFetchFrame() {
            return (this.pointerState & DrawingPointerState.FetchFrame.state()) == DrawingPointerState.FetchFrame.state();
        }
        public boolean isCalibrateToOrigin() {
            return (this.pointerState & DrawingPointerState.CalibrateToOrigin.state()) == DrawingPointerState.CalibrateToOrigin.state();
        }
        public boolean isForceFinish() {
            return (this.pointerState & DrawingPointerState.ForceFinish.state()) == DrawingPointerState.ForceFinish.state();
        }
        public boolean isVeryBegin() {
            return (this.pointerState & DrawingPointerState.VeryBegin.state()) == DrawingPointerState.VeryBegin.state();
        }
        public boolean isVeryEnd() {
            return (this.pointerState & DrawingPointerState.VeryEnd.state()) == DrawingPointerState.VeryEnd.state();
        }

        public boolean shouldEnd() {
            return isTouchUp() || isForceFinish() || isVeryEnd();
        }

        @Override
        public String toString() {
            return Integer.toHexString(this.pointerState);
        }
    }
}