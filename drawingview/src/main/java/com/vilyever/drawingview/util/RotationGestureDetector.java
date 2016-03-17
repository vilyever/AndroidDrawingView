package com.vilyever.drawingview.util;

import android.view.MotionEvent;

/**
 * RotationGestureDetector
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/22.
 * Feature:
 * 旋转手势检测
 */
public class RotationGestureDetector {
    private final RotationGestureDetector self = this;

    // 触摸ID不可用值
    private static final int INVALID_POINTER_ID = -1;

    /* #Constructors */
    public RotationGestureDetector(OnRotationGestureListener listener) {
        this.mListener = listener;
        this.firstPointerID = INVALID_POINTER_ID;
        this.secondPointerID = INVALID_POINTER_ID;
    }

    /* Public Methods */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                this.firstPointerID = event.getPointerId(event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                this.secondPointerID = event.getPointerId(event.getActionIndex());
                this.secondX = event.getX(event.findPointerIndex(this.firstPointerID));
                this.secondY = event.getY(event.findPointerIndex(this.firstPointerID));
                this.firstX = event.getX(event.findPointerIndex(this.secondPointerID));
                this.firstY = event.getY(event.findPointerIndex(this.secondPointerID));
                break;
            case MotionEvent.ACTION_MOVE:
                if(this.firstPointerID != INVALID_POINTER_ID && this.secondPointerID != INVALID_POINTER_ID) {
                    float nfX, nfY, nsX, nsY;
                    nsX = event.getX(event.findPointerIndex(this.firstPointerID));
                    nsY = event.getY(event.findPointerIndex(this.firstPointerID));
                    nfX = event.getX(event.findPointerIndex(this.secondPointerID));
                    nfY = event.getY(event.findPointerIndex(this.secondPointerID));

                    this.mAngle = angleBetweenLines(this.firstX, this.firstY, this.secondX, this.secondY, nfX, nfY, nsX, nsY);

                    if (this.mListener != null) {
                        this.mListener.onRotate(this);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                this.firstPointerID = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                this.secondPointerID = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_CANCEL:
                this.firstPointerID = INVALID_POINTER_ID;
                this.secondPointerID = INVALID_POINTER_ID;
                break;
        }
        return true;
    }

    /* Properties */
    private OnRotationGestureListener mListener;

    // 两只手指触摸id
    private int firstPointerID, secondPointerID;
    private float firstX, firstY, secondX, secondY;

    // 当前旋转角度
    private float mAngle;
    public float getAngle() {
        return this.mAngle;
    }

    /* Private Methods */
    private float angleBetweenLines (float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY) {
        float angle1 = (float) Math.atan2( (fY - sY), (fX - sX) );
        float angle2 = (float) Math.atan2( (nfY - nsY), (nfX - nsX) );

        float angle = ((float)Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return angle;
    }

    /* Interfaces */
    public interface OnRotationGestureListener {
        void onRotate(RotationGestureDetector rotationDetector);
    }
}