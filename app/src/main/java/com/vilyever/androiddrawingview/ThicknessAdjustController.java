package com.vilyever.androiddrawingview;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;

/**
 * ThicknessAdjustController
 * AndroidDrawingView <com.vilyever.androiddrawingview>
 * Created by vilyever on 2015/9/21.
 * Feature:
 */
public class ThicknessAdjustController {
    private final ThicknessAdjustController self = this;

    private ThicknessDelegate delegate;

    private ViewGroup rootView;
    private SeekBar seekBar;

    protected PopupWindow popupWindow;
    protected Size popupSize;
    
    /* #Constructors */
    public ThicknessAdjustController(Context context) {
        // Required empty public constructor

        self.rootView = (ViewGroup) View.inflate(context, R.layout.thickness_adjust_controller, null);

        self.popupSize = new Size(400, 60);

        self.seekBar = (SeekBar) self.rootView.findViewById(R.id.seekBar);
        self.seekBar.setMax(50);

        self.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (self.getDelegate() != null) {
                    self.getDelegate().thicknessDidChangeFromThicknessAdjustController(self, seekBar.getProgress() + 1);
                }
            }
        });
    }
    
    /* #Overrides */    
    
    /* #Accessors */
    public ThicknessDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(ThicknessDelegate delegate) {
        this.delegate = delegate;
    }

    public PopupWindow getPopupWindow() {
        if (self.popupWindow == null) {
            self.popupWindow = new PopupWindow(self.rootView);
            self.popupWindow.setFocusable(true);
            self.popupWindow.setBackgroundDrawable(new ColorDrawable());
        }
        return popupWindow;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public void setThickness(int thickness) {
        self.seekBar.setProgress(thickness - 1);
    }

    public void popupFromView(View v) {
        self.getPopupWindow().setWidth(self.popupSize.width);
        self.getPopupWindow().setHeight(self.popupSize.height);
        self.getPopupWindow().showAsDropDown(v, (v.getWidth() - self.popupSize.width) / 2, 0);
    }

    public void dismissPopup() {
        self.getPopupWindow().dismiss();
    }

    /* #Classes */


    /* #Interfaces */
    public interface ThicknessDelegate {
        void thicknessDidChangeFromThicknessAdjustController(ThicknessAdjustController controller, int thickness);
    }
     
    /* #Annotations @interface */    
    
    /* #Enums */
}