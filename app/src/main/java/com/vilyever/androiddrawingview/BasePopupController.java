package com.vilyever.androiddrawingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.vilyever.unitconversion.DimenConverter;


/**
 * BasePopupController
 * ESB <com.vilyever.operationbar>
 * Created by vilyever on 2015/11/23.
 * Feature:
 */
public abstract class BasePopupController extends BaseController {
    final BasePopupController self = this;

    private Delegate delegate;

    protected PopupWindow popupWindow;
    protected int preferWidth;
    protected int preferHeight;

    protected PopupBackgroundView popupBackgroundView;
    protected int popupBackgroundColor;
    protected int popupRoundedRadius;

    /* #Constructors */
    public BasePopupController(Context context) {
        super(context);
    }
    
    /* #Overrides */    
    
    /* #Accessors */
    public <T extends Delegate> T getDelegate() {
        return (T) delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public PopupWindow getPopupWindow() {
        if (self.popupWindow == null) {
            self.popupWindow = new PopupWindow(self.getPopupBackgroundView());
            self.popupWindow.setFocusable(true);
            self.popupWindow.setClippingEnabled(false);
            self.popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
            self.popupWindow.setBackgroundDrawable(new ColorDrawable());

            self.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    self.popupBackgroundView.removeAllViews();

                    if (self.getDelegate() != null) {
                        self.getDelegate().popupWindowDidDismiss(self);
                    }
                }
            });
        }
        return popupWindow;
    }

    public int getPreferWidth() {
        return preferWidth;
    }

    public void setPreferWidth(int preferWidth) {
        this.preferWidth = preferWidth;
    }

    public int getPreferHeight() {
        return preferHeight;
    }

    public void setPreferHeight(int preferHeight) {
        this.preferHeight = preferHeight;
    }

    public PopupBackgroundView getPopupBackgroundView() {
        if (self.popupBackgroundView == null) {
            self.popupBackgroundView = new PopupBackgroundView(self.getContext());
            self.popupBackgroundView.setBackgroundColor(self.getPopupBackgroundColor());
        }
        return popupBackgroundView;
    }

    public int getPopupBackgroundColor() {
        return popupBackgroundColor;
    }

    public void setPopupBackgroundColor(int popupBackgroundColor) {
        this.popupBackgroundColor = popupBackgroundColor;
        self.getPopupBackgroundView().setBackgroundColor(popupBackgroundColor);
    }

    public int getPopupRoundedRadius() {
        return popupRoundedRadius;
    }

    public void setPopupRoundedRadius(int popupRoundedRadius) {
        this.popupRoundedRadius = popupRoundedRadius;
        self.getPopupBackgroundView().setRoundedRadius(popupRoundedRadius);
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public void popupFromView(@NonNull View v, @NonNull PopupDirection popupDirection, boolean withArrow, int offsetX, int offsetY) {
        if (self.getPopupWindow().isShowing()) {
            return;
        }

        self.getPopupBackgroundView().addView(self.rootView);
        self.getPopupBackgroundView().setBackgroundColor(self.getPopupBackgroundColor());
        self.getPopupBackgroundView().setRoundedRadius(self.getPopupRoundedRadius());

        int popupWidth = self.preferWidth;
        int popupHeight = self.preferHeight;

        if (withArrow) {
            self.getPopupBackgroundView().setArrowDirection(popupDirection);
            switch (popupDirection) {
                case Left:
                case Right:
                    popupWidth += PopupBackgroundView.ArrowRadius;
                    break;
                case Up:
                case Down:
                    popupHeight += PopupBackgroundView.ArrowRadius;
                    break;
            }
        }
        else {
            self.getPopupBackgroundView().setArrowDirection(PopupDirection.None);
        }

        self.getPopupWindow().setWidth(popupWidth);
        self.getPopupWindow().setHeight(popupHeight);

        int[] location = new int[2];
        v.getLocationInWindow(location);
        int originX = location[0];
        int originY = location[1];

        switch (popupDirection) {
            case None:
                self.getPopupWindow().showAtLocation(v, Gravity.START | Gravity.TOP, offsetX, offsetY);
                break;
            case Left:
                self.getPopupWindow().showAtLocation(v,
                        Gravity.START | Gravity.TOP,
                        originX - popupWidth + PopupBackgroundView.ShadowRadius + offsetX,
                        originY - (popupHeight / 2 - v.getHeight() / 2) + offsetY);
                break;
            case Up:
                self.getPopupWindow().showAtLocation(v,
                        Gravity.START | Gravity.TOP,
                        originX - (popupWidth / 2 - v.getWidth() / 2) + offsetX,
                        originY - popupHeight + PopupBackgroundView.ShadowRadius + offsetY);
                break;
            case Right:
                self.getPopupWindow().showAtLocation(v,
                        Gravity.START | Gravity.TOP,
                        originX + v.getWidth() - PopupBackgroundView.ShadowRadius + offsetX,
                        originY - (popupHeight / 2 - v.getHeight() / 2) + offsetY);
                break;
            case Down:
                self.getPopupWindow().showAtLocation(v,
                        Gravity.START | Gravity.TOP,
                        originX - (popupWidth / 2 - v.getWidth() / 2) + offsetX,
                        originY + v.getHeight() - PopupBackgroundView.ShadowRadius + offsetY);
                break;
        }
    }

    public void dismissPopup() {
        if (!self.getPopupWindow().isShowing()) {
            return;
        }
        self.getPopupWindow().dismiss();
    }

    /* #Classes */
    private static class PopupBackgroundView extends RelativeLayout {
        final PopupBackgroundView self = this;

        public static final int ShadowRadius = DimenConverter.dpToPixel(3);
        public static final int ArrowRadius = DimenConverter.dpToPixel(16);

        private int backgroundColor = Color.WHITE;
        private int roundedRadius;
        private PopupDirection arrowDirection;

        /* #Constructors */
        public PopupBackgroundView(Context context) {
            super(context);
            self.initial();
        }

        public PopupBackgroundView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public PopupBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            int[] attrsArray = new int[] {
                    android.R.attr.background,
            };

            final TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

            try {
                Drawable background = ta.getDrawable(0);
                if (background instanceof ColorDrawable) {
                    self.backgroundColor = ((ColorDrawable) background).getColor();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            self.initial();
        }

        /* #Overrides */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int triangleLength = (int) (ArrowRadius * Math.tan(Math.PI / 6) * 2);

            int centerX = self.getWidth() / 2;
            int centerY = self.getHeight() / 2;

            int left = ShadowRadius;
            int top = ShadowRadius;
            int right = self.getWidth() - ShadowRadius;
            int bottom = self.getHeight() - ShadowRadius;

            RectF contentRect = new RectF(left, top, right, bottom);

            switch (self.getArrowDirection()) {
                case None:
                    break;
                case Left:
                    contentRect.right -= ArrowRadius;
                    break;
                case Up:
                    contentRect.bottom -= ArrowRadius;
                    break;
                case Right:
                    contentRect.left += ArrowRadius;
                    break;
                case Down:
                    contentRect.top += ArrowRadius;
                    break;
            }

//        float width = right - left;
//        float height = baseLineBottom - top;
//        double atan = Math.atan(width / height);
//        double aa = Math.sqrt(2);
//        double bb = (2 - aa) * Math.PI / 4;
//        int roundRectRadius = (int) (Math.sqrt(width + height) * Math.sin(aa * atan + bb));

            RectF arcRect = new RectF(0, 0, self.roundedRadius * 2, self.roundedRadius * 2);

            Path path = new Path();

            path.moveTo(contentRect.left + self.roundedRadius, contentRect.top);

            if (self.getArrowDirection() == PopupDirection.Down) {
                path.lineTo(centerX - triangleLength / 2, contentRect.top);
                path.lineTo(centerX, top);
                path.lineTo(centerX + triangleLength / 2, contentRect.top);
            }
            path.lineTo(contentRect.right - self.roundedRadius, contentRect.top);
            arcRect.offsetTo(contentRect.right - self.roundedRadius * 2, contentRect.top);
            path.arcTo(arcRect, 270.0f, 90.0f);

            if (self.getArrowDirection() == PopupDirection.Left) {
                path.lineTo(contentRect.right, centerY - triangleLength / 2);
                path.lineTo(right, centerY);
                path.lineTo(contentRect.right, centerY + triangleLength / 2);
            }
            path.lineTo(contentRect.right, contentRect.bottom - self.roundedRadius);
            arcRect.offsetTo(contentRect.right - self.roundedRadius * 2, contentRect.bottom - self.roundedRadius * 2);
            path.arcTo(arcRect, 0.0f, 90.0f);

            if (self.getArrowDirection() == PopupDirection.Up) {
                path.lineTo(centerX - triangleLength / 2, contentRect.bottom);
                path.lineTo(centerX, bottom);
                path.lineTo(centerX + triangleLength / 2, contentRect.bottom);
            }
            path.lineTo(contentRect.left + self.roundedRadius, contentRect.bottom);
            arcRect.offsetTo(contentRect.left, contentRect.bottom - self.roundedRadius * 2);
            path.arcTo(arcRect, 90.0f, 90.0f);

            if (self.getArrowDirection() == PopupDirection.Right) {
                path.lineTo(contentRect.left, centerY - triangleLength / 2);
                path.lineTo(left, centerY);
                path.lineTo(contentRect.left, centerY + triangleLength / 2);
            }
            path.lineTo(contentRect.left, contentRect.top + self.roundedRadius);
            arcRect.offsetTo(contentRect.left, contentRect.top);
            path.arcTo(arcRect, 180.0f, 90.0f);

            Paint backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
            backgroundPaint.setDither(true);
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(self.backgroundColor);
                backgroundPaint.setShadowLayer(ShadowRadius, 0, 0, Color.DKGRAY);

            canvas.drawPath(path, backgroundPaint);
        }

        @Override
        public void setBackground(Drawable background) {
        }

        @Override
        public void setBackgroundColor(int color) {
            if (color == Color.TRANSPARENT) {
                color = Color.WHITE;
            }
            self.backgroundColor = color;
            self.invalidate();
        }

        /* #Accessors */
        public int getRoundedRadius() {
            return roundedRadius;
        }

        public void setRoundedRadius(int roundedRadius) {
            this.roundedRadius = roundedRadius;
            self.invalidate();
        }

        public PopupDirection getArrowDirection() {
            if (self.arrowDirection == null) {
                return PopupDirection.None;
            }
            return arrowDirection;
        }

        public void setArrowDirection(PopupDirection arrowDirection) {
            this.arrowDirection = arrowDirection;

            switch (arrowDirection) {
                case None:
                    self.setPadding(0, 0, 0, 0);
                    break;
                case Left:
                    self.setPadding(0, 0, ArrowRadius, 0);
                    break;
                case Up:
                    self.setPadding(0, 0, 0, ArrowRadius);
                    break;
                case Right:
                    self.setPadding(ArrowRadius, 0, 0, 0);
                    break;
                case Down:
                    self.setPadding(0, ArrowRadius, 0, 0);
                    break;
            }

            self.invalidate();
        }

        /* #Delegates */

        /* #Private Methods */
        private void initial() {
            self.setWillNotDraw(false);
            self.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            self.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        /* #Public Methods */

        /* #Classes */

        /* #Interfaces */

        /* #Annotations @interface */

        /* #Enums */

    }

    /* #Interfaces */
    public interface Delegate {
        void popupWindowDidDismiss(BasePopupController controller);
    }
     
    /* #Annotations @interface */    
    
    /* #Enums */
    public enum PopupDirection {
        None, Left, Up, Right, Down;
    }
}