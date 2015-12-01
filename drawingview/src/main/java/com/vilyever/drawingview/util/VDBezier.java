package com.vilyever.drawingview.util;

import com.vilyever.drawingview.model.VDDrawingPoint;

/**
 * VDBezier
 * AndroidDrawingView <com.vilyever.drawingview.util>
 * Created by vilyever on 2015/11/13.
 * Feature:
 */
public class VDBezier {
    final VDBezier self = this;

    public VDDrawingPoint startPoint;
    public VDDrawingPoint secondPoint;
    public VDDrawingPoint endPoint;

    /* #Constructors */
    public VDBezier(VDDrawingPoint startPoint, VDDrawingPoint secondPoint, VDDrawingPoint endPoint) {
        this.startPoint = startPoint;
        this.secondPoint = secondPoint;
        this.endPoint = endPoint;
    }

    /* #Overrides */

    /* #Accessors */

    /* #Delegates */

    /* #Private Methods */

    /* #Public Methods */
    // Bezier曲线长度
    public float length() {
        int steps = 10, length = 0;
        int i;
        float t;
        double cx, cy, px = 0, py = 0, xdiff, ydiff;

        for (i = 0; i <= steps; i++) {
            t = i / steps;
            cx = VDBezier.point(t, this.startPoint.x, this.secondPoint.x, this.endPoint.x);
            cy = VDBezier.point(t, this.startPoint.y, this.secondPoint.y, this.endPoint.y);
            if (i > 0) {
                xdiff = cx - px;
                ydiff = cy - py;
                length += Math.sqrt(xdiff * xdiff + ydiff * ydiff);
            }
            px = cx;
            py = cy;
        }
        return length;
    }

    // 二次方贝塞尔曲线
    // 计算t比例（0<=t<=1)时，曲线由起始点向终点运动的坐标
    // 分别传入x,y求解
    public static double point(float t, float start, float second, float end) {
        return start * (1.0 - t) * (1.0 - t)
                + 2.0 *  second   * (1.0 - t)  * t
                +        end      * t          * t;
    }

//    // 三次方贝塞尔曲线
//    // 计算t比例（0<=t<=1)时，曲线由起始点向终点运动的坐标
//    // 分别传入x,y求解
//    public double point(float t, float start, float second, float third, float end) {
//        return start * (1.0 - t) * (1.0 - t)  * (1.0 - t)
//                + 3.0 *  second   * (1.0 - t)  * (1.0 - t)  * t
//                + 3.0 *  third    * (1.0 - t)  * t          * t
//                +        end      * t          * t          * t;
//    }

    /* #Classes */

    /* #Interfaces */

    /* #Annotations @interface */

    /* #Enums */
}