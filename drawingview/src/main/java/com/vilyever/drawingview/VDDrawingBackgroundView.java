package com.vilyever.drawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * VDDrawingLayerImageView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 */
public class VDDrawingBackgroundView extends SurfaceView implements Runnable {
    private final VDDrawingBackgroundView self = this;

    private Thread drawingThread;
    private boolean drawingThreadRunning;
    private boolean shouldDraw;

    private Drawable backgroundDrawable;

    /* #Constructors */
    public VDDrawingBackgroundView(Context context) {
        super(context);
        self.init(context);
    }

    /* #Overrides */

    /* #Accessors */

    /* #Delegates */
    // Runnable
    @Override
    public void run() {
        while (self.drawingThreadRunning) {
            if (self.getHolder().getSurface().isValid()) {
                if (self.shouldDraw) {
                    Canvas canvas = self.getHolder().lockCanvas();
                    if (canvas != null) {
                        self.shouldDraw = false;

                        if (self.backgroundDrawable != null) {
                            self.backgroundDrawable.setBounds(0, 0, self.getWidth(), self.getHeight());
                            self.backgroundDrawable.draw(canvas);
                        }
                        else {
                            canvas.drawColor(Color.WHITE); // default color
                        }

                        self.getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    /* #Private Methods */
    private void init(Context context) {
        self.setZOrderOnTop(false);
        self.getHolder().setFormat(PixelFormat.TRANSPARENT);

        self.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                self.drawingThreadRunning = true;
                if (self.drawingThread == null) {
                    self.drawingThread = new Thread(self);
                    self.drawingThread.start();
                }
                self.shouldDraw = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                self.shouldDraw = true;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                self.drawingThreadRunning = false;
                if (self.drawingThread != null) {
                    self.drawingThread.interrupt();
                    self.drawingThread = null;
                }
            }
        });
    }

    /* #Public Methods */
    public void updateBackground(Drawable drawable) {
        self.backgroundDrawable = drawable;
        self.shouldDraw = true;
    }

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}