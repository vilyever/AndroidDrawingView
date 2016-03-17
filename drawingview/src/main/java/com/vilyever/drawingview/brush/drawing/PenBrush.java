package com.vilyever.drawingview.brush.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;

import com.vilyever.drawingview.R;
import com.vilyever.drawingview.model.DrawingPath;
import com.vilyever.drawingview.model.DrawingPoint;
import com.vilyever.resource.Resource;

/**
 * PenBrush
 * AndroidDrawingView <com.vilyever.drawingview.brush>
 * Created by vilyever on 2015/10/20.
 * Feature:
 * 任意点绘制，手指跟随
 * 贝塞尔平滑
 */
public class PenBrush extends DrawingBrush {
    final PenBrush self = this;

    /* Constructors */
    public PenBrush() {

    }

    public PenBrush(float size, int color) {
        super(size, color);
    }

    /* Public Methods */
    public static PenBrush defaultBrush() {
        return new PenBrush(Resource.getDimensionPixelSize(R.dimen.drawingViewBrushDefaultSize), Color.BLACK);
    }

    /* Overrides */
    @Override
    protected void updatePaint() {
        super.updatePaint();

        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeCap(Paint.Cap.ROUND);
        getPaint().setStrokeJoin(Paint.Join.ROUND);
        getPaint().setStrokeMiter(0);
    }

    @NonNull
    @Override
    public Frame drawPath(Canvas canvas, @NonNull DrawingPath drawingPath, @NonNull DrawingState state) {
        updatePaint();
        if (drawingPath.getPoints().size() > 0) {
            Frame pathFrame = super.drawPath(canvas, drawingPath, state);

            if (state.isFetchFrame() || canvas == null) {
                return pathFrame;
            }

            DrawingPoint beginPoint = drawingPath.getPoints().get(0);
            DrawingPoint lastPoint = drawingPath.getPoints().get(drawingPath.getPoints().size() - 1);

            Path path = new Path();
            if (drawingPath.getPoints().size() == 1) {
                getPaint().setStyle(Paint.Style.FILL);
                path.addCircle(beginPoint.getX(), beginPoint.getY(), getSize() / 2.0f, Path.Direction.CW);
            }
            else if (drawingPath.getPoints().size() > 1) {
                path.moveTo(beginPoint.getX(), beginPoint.getY());
                for (int i = 1; i < drawingPath.getPoints().size(); i++) {
                    DrawingPoint prePoint = drawingPath.getPoints().get(i - 1);
                    DrawingPoint currentPoint = drawingPath.getPoints().get(i);

                    double s = Math.sqrt(Math.pow(currentPoint.getX() - prePoint.getX(), 2) + Math.pow(currentPoint.getY() - prePoint.getY(), 2));

                    if (s < 2) { // 往复颤抖间距估值，高分辨率上使用quadTo会出现异常绘画，反复在一点来回抖动会出现毛刺
                        path.lineTo(currentPoint.getX(), currentPoint.getY());
                    }
                    else {
                        path.quadTo(prePoint.getX(), prePoint.getY(),
                                     (prePoint.getX() + currentPoint.getX()) / 2.0f, (prePoint.getY() + currentPoint.getY()) / 2.0f);
                    }

                    if (state.shouldEnd()) {
                        if (i == drawingPath.getPoints().size() - 1) {
                            path.quadTo((prePoint.getX() + currentPoint.getX()) / 2.0f, (prePoint.getY() + currentPoint.getY()) / 2.0f,
                                    currentPoint.getX(), currentPoint.getY());
                        }
                    }
                }
            }

            if (state.isCalibrateToOrigin()) {
                path.offset(-pathFrame.left, -pathFrame.top);
            }

            canvas.drawPath(path, getPaint());

            return pathFrame;
        }

        return Frame.EmptyFrame();
    }

    @Override
    public boolean shouldDrawFromBegin() {
        return false;
    }
}