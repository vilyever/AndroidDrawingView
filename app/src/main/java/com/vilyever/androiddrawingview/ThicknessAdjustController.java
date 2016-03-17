package com.vilyever.androiddrawingview;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.vilyever.unitconversion.DimenConverter;

/**
 * ThicknessAdjustController
 * AndroidDrawingView <com.vilyever.androiddrawingview>
 * Created by vilyever on 2015/9/21.
 * Feature:
 */
public class ThicknessAdjustController extends BasePopupController {
    private final ThicknessAdjustController self = this;

    private ThicknessDelegate thicknessDelegate;

    private SeekBar seekBar;

    /* #Constructors */
    public ThicknessAdjustController(Context context) {
        super(context);

        self.rootView = View.inflate(context, R.layout.thickness_adjust_controller, null);
        self.rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        self.preferWidth = DimenConverter.dpToPixel(250);
        self.preferHeight = DimenConverter.dpToPixel(50);

        self.setPopupBackgroundColor(Color.BLACK);

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
                if (self.getThicknessDelegate() != null) {
                    self.getThicknessDelegate().thicknessDidChangeFromThicknessAdjustController(self, seekBar.getProgress() + 1);
                }
            }
        });
    }
    
    /* #Overrides */    
    
    /* #Accessors */
    public ThicknessDelegate getThicknessDelegate() {
        return thicknessDelegate;
    }

    public void setThicknessDelegate(ThicknessDelegate thicknessDelegate) {
        this.thicknessDelegate = thicknessDelegate;
    }

    /* #Delegates */
     
    /* #Private Methods */    
    
    /* #Public Methods */
    public void setThickness(int thickness) {
        self.seekBar.setProgress(thickness - 1);
    }

    /* #Classes */


    /* #Interfaces */
    public interface ThicknessDelegate {
        void thicknessDidChangeFromThicknessAdjustController(ThicknessAdjustController controller, int thickness);
    }
     
    /* #Annotations @interface */    
    
    /* #Enums */
}