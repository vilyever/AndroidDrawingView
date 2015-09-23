package com.vilyever.androiddrawingview;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vilyever.drawingview.VDDrawingBrush;
import com.vilyever.drawingview.VDDrawingView;

import java.util.ArrayList;
import java.util.List;

/**
 * DrawingFragment
 * AndroidDrawingBoard <com.vilyever.androiddrawingboard>
 * Created by vilyever on 2015/9/21.
 * Feature:
 */
public class DrawingFragment extends Fragment {
    private final DrawingFragment self = this;

    private VDDrawingView drawingView;

    private Button undoButton;
    private Button redoButton;
    private Button clearButton;

    private Button penButton;
    private Button eraserButton;
    private Button shapeButton;
    private Button backgroundColorButton;

    private Button thicknessButton;
    private Button colorButton;
    private Button fillColorButton;
    private Button oneStrokeOneLayerButton;

    private ThicknessAdjustController thicknessAdjustController;

    private List<Button> singleSelectionButtons;

    /* #Constructors */
    public DrawingFragment() {
        // Required empty public constructor
    }


    /* #Overrides */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.drawing_fragment, container, false);

        self.drawingView = (VDDrawingView) rootView.findViewById(R.id.drawingView);
        self.drawingView.setDelegate(new VDDrawingView.DrawingDelegate() {
            @Override
            public void undoStateDidChangeFromDrawingView(VDDrawingView drawingView, boolean canUndo, boolean canRedo) {
                self.undoButton.setEnabled(canUndo);
                self.redoButton.setEnabled(canRedo);
            }
        });

        self.undoButton = (Button) rootView.findViewById(R.id.undoButton);
        self.undoButton.setEnabled(false);
        self.undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.drawingView.undo();
            }
        });

        self.redoButton = (Button) rootView.findViewById(R.id.redoButton);
        self.redoButton.setEnabled(false);
        self.redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.drawingView.redo();
            }
        });

        self.clearButton = (Button) rootView.findViewById(R.id.clearButton);
        self.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.drawingView.clear();
            }
        });

        self.penButton = (Button) rootView.findViewById(R.id.penButton);
        self.penButton.setSelected(true);
        self.penButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.selectButton(self.singleSelectionButtons, self.penButton);
                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setShape(VDDrawingBrush.Shape.None));
            }
        });

        self.eraserButton = (Button) rootView.findViewById(R.id.eraserButton);
        self.eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.selectButton(self.singleSelectionButtons, self.eraserButton);
                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setShape(VDDrawingBrush.Shape.Eraser));
            }
        });

        self.shapeButton = (Button) rootView.findViewById(R.id.shapeButton);

        self.backgroundColorButton = (Button) rootView.findViewById(R.id.backgroundColorButton);

        self.thicknessButton = (Button) rootView.findViewById(R.id.thicknessButton);
        self.thicknessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.getThicknessAdjustController().setThickness((int) self.drawingView.getDrawingBrush().getSize());
                self.getThicknessAdjustController().popupFromView(v);
            }
        });

        self.colorButton = (Button) rootView.findViewById(R.id.colorButton);

        self.fillColorButton = (Button) rootView.findViewById(R.id.fillColorButton);

        self.oneStrokeOneLayerButton = (Button) rootView.findViewById(R.id.oneStrokeOneLayerButton);
        self.oneStrokeOneLayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setOneStrokeToLayer(v.isSelected()));
            }
        });

        self.singleSelectionButtons = new ArrayList<>();
        self.singleSelectionButtons.add(self.penButton);
        self.singleSelectionButtons.add(self.eraserButton);
        self.singleSelectionButtons.add(self.shapeButton);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        drawingView.destroy();
    }

    
    /* #Accessors */

    public ThicknessAdjustController getThicknessAdjustController() {
        if (self.thicknessAdjustController == null) {
            self.thicknessAdjustController = new ThicknessAdjustController(self.getActivity());
            self.thicknessAdjustController.setDelegate(new ThicknessAdjustController.ThicknessDelegate() {
                @Override
                public void thicknessDidChangeFromThicknessAdjustController(ThicknessAdjustController controller, int thickness) {
                    self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setSize(thickness));
                }
            });
        }
        return thicknessAdjustController;
    }
    /* #UI Actions */
    
    /* #Delegates */     
     
    /* #Private Methods */
    private void selectButton(List<Button> buttons, Button button) {
        for (Button b : buttons) {
            b.setSelected(b == button);
        }
    }
    
    /* #Public Methods */

    /* #Classes */

    /* #Interfaces */     
     
    /* #Annotations @interface */    
    
    /* #Enums */
}