package com.vilyever.drawingview.brush;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
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

    public static <T extends VDBrush> T copy(@NonNull VDBrush brush) {
        return (T) new VDJson<>(brush.getClass()).modelFromJson(brush.toJson());
    }

    /**
     *
     * @param canvas the canvas in drawing
     * @param drawingPath the path will draw
     * @param state drawing state
     * @return 绘制图形所处的Frame，不同state可能返回不同坐标，若返回EmptyFrame表示path不足以作图，若返回requireMoreDetail为true表示当前path不能完成作图，需要进一步触摸绘制
     */
    @NonNull
    public abstract Frame drawPath(Canvas canvas, @NonNull VDDrawingPath drawingPath, @NonNull DrawingState state);

    /**
     *
     * @return 同一笔绘图时是否在path变更时消除先前作图
     */
    public boolean shouldDrawFromBegin() {
        return true;
    }

    /**
     * 当前绘制比例
     * 在记录数据时drawingView的宽高和当前重绘时的宽高比
     * 用于在不同分辨率下重绘相似的图形全貌
     *
     * 在brush中此比例不分xy轴，取xy轴中变化最大的一轴
     */
    @VDJsonKeyIgnore
    private float drawingRatio = 1.0f;
    public VDBrush setDrawingRatio(float drawingRatio) {
        this.drawingRatio = drawingRatio;
        return this;
    }
    public float getDrawingRatio() {
        return drawingRatio;
    }

    /* #Classes */
    public static class Frame extends RectF {
        public boolean requireMoreDetail;

        public Frame() {

        }

        public Frame(float left, float top, float right, float bottom) {
            super(left, top, right, bottom);
        }

        public Frame(RectF r) {
            super(r);
        }

        public Frame(Rect r) {
            super(r);
        }

        public Frame(Frame f) {
            super(f);
        }

        public static Frame EmptyFrame() {
            return new Frame(-1, -1, -1, -1);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (requireMoreDetail ? 1 : 0));
        }

        public static final Parcelable.Creator<Frame> CREATOR = new Parcelable.Creator<Frame>() {
            public Frame createFromParcel(Parcel in) {
                Frame f = new Frame();
                f.readFromParcel(in);
                return f;
            }

            /**
             * Return an array of rectangles of the specified size.
             */
            public Frame[] newArray(int size) {
                return new Frame[size];
            }
        };

        public void readFromParcel(Parcel in) {
            super.readFromParcel(in);
            requireMoreDetail = in.readByte() != 0;
        }
    }

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