package com.vilyever.androiddrawingview;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vilyever.drawingview.VDDrawingBrush;
import com.vilyever.drawingview.VDDrawingView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private Button solidColorButton;
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
        self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setColor(Color.YELLOW));
        self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setSize(35));
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
                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setType(VDDrawingBrush.Type.Pen));
            }
        });

        self.eraserButton = (Button) rootView.findViewById(R.id.eraserButton);
        self.eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.selectButton(self.singleSelectionButtons, self.eraserButton);
                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setType(VDDrawingBrush.Type.Eraser));
            }
        });

        self.shapeButton = (Button) rootView.findViewById(R.id.shapeButton);
        self.shapeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VDDrawingBrush.Shape shape = VDDrawingBrush.Shape.Line;
                if (v.getTag() != null) {
                    shape = (VDDrawingBrush.Shape) v.getTag();
                }

                if (!v.isSelected()) {
                    self.selectButton(self.singleSelectionButtons, (Button) v);
                    self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setType(VDDrawingBrush.Type.Shape));
                }
                else {
                    switch (shape) {
                        case Polygon:
                            shape = VDDrawingBrush.Shape.Line;
                            break;
                        case Line:
                            shape = VDDrawingBrush.Shape.Rectangle;
                            break;
                        case Rectangle:
                            shape = VDDrawingBrush.Shape.RoundedRetangle;
                            break;
                        case RoundedRetangle:
                            shape = VDDrawingBrush.Shape.Circle;
                            break;
                        case Circle:
                            shape = VDDrawingBrush.Shape.Ellipse;
                            break;
                        case Ellipse:
                            shape = VDDrawingBrush.Shape.RightAngledRriangle;
                            break;
                        case RightAngledRriangle:
                            shape = VDDrawingBrush.Shape.IsoscelesTriangle;
                            break;
                        case IsoscelesTriangle:
                            shape = VDDrawingBrush.Shape.Rhombus;
                            break;
                        case Rhombus:
                            shape = VDDrawingBrush.Shape.CenterCircle;
                            break;
                        case CenterCircle:
                            shape = VDDrawingBrush.Shape.Polygon;
                            break;
                        default:
                            shape = VDDrawingBrush.Shape.Polygon;
                            break;
                    }

                }

                v.setTag(shape);
                ((Button) v).setText(shape.name());

                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setShape(shape));
            }
        });

        self.backgroundColorButton = (Button) rootView.findViewById(R.id.backgroundColorButton);
        self.backgroundColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                self.drawingView.setBackgroundColor(Color.argb(255, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256), 0);
            }
        });

        self.thicknessButton = (Button) rootView.findViewById(R.id.thicknessButton);
        self.thicknessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.getThicknessAdjustController().setThickness((int) self.drawingView.getDrawingBrush().getSize());
                self.getThicknessAdjustController().popupFromView(v);
            }
        });

        self.colorButton = (Button) rootView.findViewById(R.id.colorButton);
        self.colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setColor(Color.argb(Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256)));
            }
        });

        self.solidColorButton = (Button) rootView.findViewById(R.id.solidColorButton);
        self.solidColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                self.drawingView.setDrawingBrush(self.drawingView.getDrawingBrush().setSolidColor(Color.argb(Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256)));
            }
        });

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