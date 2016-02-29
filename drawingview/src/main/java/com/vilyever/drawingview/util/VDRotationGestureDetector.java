package com.vilyever.drawingview.util;

import android.view.MotionEvent;

/**
 * VDRotationGestureDetector
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/22.
 * Feature:
 * 旋转手势检测
 */
public class VDRotationGestureDetector {
    private final VDRotationGestureDetector self = this;

    // 触摸ID不可用值
    private static final int INVALID_POINTER_ID = -1;

    /* #Constructors */
    public VDRotationGestureDetector(OnRotationGestureListener listener) {
        mListener = listener;
        firstPointerID = INVALID_POINTER_ID;
        secondPointerID = INVALID_POINTER_ID;
    }

    /* Public Methods */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                firstPointerID = event.getPointerId(event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                secondPointerID = event.getPointerId(event.getActionIndex());
                secondX = event.getX(event.findPointerIndex(firstPointerID));
                secondY = event.getY(event.findPointerIndex(firstPointerID));
                firstX = event.getX(event.findPointerIndex(secondPointerID));
                firstY = event.getY(event.findPointerIndex(secondPointerID));
                break;
            case MotionEvent.ACTION_MOVE:
                if(firstPointerID != INVALID_POINTER_ID && secondPointerID != INVALID_POINTER_ID) {
                    float nfX, nfY, nsX, nsY;
                    nsX = event.getX(event.findPointerIndex(firstPointerID));
                    nsY = event.getY(event.findPointerIndex(firstPointerID));
                    nfX = event.getX(event.findPointerIndex(secondPointerID));
                    nfY = event.getY(event.findPointerIndex(secondPointerID));

                    mAngle = angleBetweenLines(firstX, firstY, secondX, secondY, nfX, nfY, nsX, nsY);

                    if (mListener != null) {
                        mListener.onRotate(this);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                firstPointerID = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                secondPointerID = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_CANCEL:
                firstPointerID = INVALID_POINTER_ID;
                secondPointerID = INVALID_POINTER_ID;
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
        return mAngle;
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
        void onRotate(VDRotationGestureDetector rotationDetector);
    }
}