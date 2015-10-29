package com.vilyever.androiddrawingview;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.vilyever.contextholder.VDContextHolder;
import com.vilyever.drawingview.VDDrawingView;
import com.vilyever.drawingview.brush.VDCenterCircleBrush;
import com.vilyever.drawingview.brush.VDCircleBrush;
import com.vilyever.drawingview.brush.VDDrawingBrush;
import com.vilyever.drawingview.brush.VDEllipseBrush;
import com.vilyever.drawingview.brush.VDIsoscelesTriangleBrush;
import com.vilyever.drawingview.brush.VDLineBrush;
import com.vilyever.drawingview.brush.VDPenBrush;
import com.vilyever.drawingview.brush.VDPolygonBrush;
import com.vilyever.drawingview.brush.VDRectangleBrush;
import com.vilyever.drawingview.brush.VDRhombusBrush;
import com.vilyever.drawingview.brush.VDRightAngledTriangleBrush;
import com.vilyever.drawingview.brush.VDRoundedRectangleBrush;
import com.vilyever.drawingview.brush.VDShapeBrush;
import com.vilyever.drawingview.brush.VDTextBrush;
import com.vilyever.unitconversion.VDDimenConversion;

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
    private Button shapeButton;
    private Button textButton;
    private Button backgroundColorButton;

    private Button thicknessButton;
    private Button eraserButton;
    private Button colorButton;
    private Button solidColorButton;
    private Button edgeRoundedButton;
    private Button oneStrokeOneLayerButton;

    private ThicknessAdjustController thicknessAdjustController;

    private List<Button> singleSelectionButtons;

    private List<VDShapeBrush> shapeBrushes = new ArrayList<>();
    private VDPenBrush penBrush;
    private VDTextBrush textBrush;

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

        String toast = "1dp = " + VDDimenConversion.dpToPixel(1) + "px";
        Toast.makeText(VDContextHolder.getContext(), toast, Toast.LENGTH_LONG);


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

        self.penBrush = VDPenBrush.defaultBrush();
        self.drawingView.setBrush(self.penBrush);
        self.penButton = (Button) rootView.findViewById(R.id.penButton);
        self.penButton.setSelected(true);
        self.penButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.selectButton(self.singleSelectionButtons, self.penButton);
                self.drawingView.setBrush(self.penBrush);
            }
        });

        self.shapeBrushes.add(VDPolygonBrush.defaultBrush());
        self.shapeBrushes.add(VDLineBrush.defaultBrush());
        self.shapeBrushes.add(VDRectangleBrush.defaultBrush());
        self.shapeBrushes.add(VDRoundedRectangleBrush.defaultBrush());
        self.shapeBrushes.add(VDCircleBrush.defaultBrush());
        self.shapeBrushes.add(VDEllipseBrush.defaultBrush());
        self.shapeBrushes.add(VDRightAngledTriangleBrush.defaultBrush());
        self.shapeBrushes.add((VDShapeBrush) VDIsoscelesTriangleBrush.defaultBrush());
        self.shapeBrushes.add(VDRhombusBrush.defaultBrush());
        self.shapeBrushes.add(VDCenterCircleBrush.defaultBrush());
        self.shapeButton = (Button) rootView.findViewById(R.id.shapeButton);
        self.shapeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VDDrawingBrush brush = null;

                if (!v.isSelected()) {
                    if (v.getTag() == null) {
                        v.setTag(1);
                    }
                    self.selectButton(self.singleSelectionButtons, (Button) v);
                } else {
                    int index = (int) v.getTag() + 1;
                    index = index % self.shapeBrushes.size();
                    v.setTag(index);
                }

                brush = self.shapeBrushes.get((Integer) v.getTag());
                self.drawingView.setBrush(brush);
                String name = brush.getClass().getSimpleName().substring(2);
                name = name.substring(0, name.length() - 5);
                ((Button) v).setText(name);
            }
        });

        self.textBrush = VDTextBrush.defaultBrush();
        self.textButton = (Button) rootView.findViewById(R.id.textButton);
        self.textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.selectButton(self.singleSelectionButtons, self.textButton);
                self.drawingView.setBrush(self.textBrush);
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
                self.getThicknessAdjustController().setThickness((int) self.penBrush.getSize());
                self.getThicknessAdjustController().popupFromView(v);
            }
        });

        self.eraserButton = (Button) rootView.findViewById(R.id.eraserButton);
        self.eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                self.penBrush.setIsEraser(v.isSelected());
                for (VDDrawingBrush brush : self.shapeBrushes) {
                    brush.setIsEraser(v.isSelected());
                }
            }
        });

        self.colorButton = (Button) rootView.findViewById(R.id.colorButton);
        self.colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int color = Color.argb(Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256);
                ((Button) v).setTextColor(color);

                self.penBrush.setColor(color);
                self.textBrush.setColor(color);
                for (VDDrawingBrush brush : self.shapeBrushes) {
                    brush.setColor(color);
                }
            }
        });

        self.solidColorButton = (Button) rootView.findViewById(R.id.solidColorButton);
        self.solidColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int color = Color.argb(Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256);
                ((Button) v).setTextColor(color);

                for (VDShapeBrush brush : self.shapeBrushes) {
                    brush.setSolidColor(color);
                }
            }
        });

        self.edgeRoundedButton = (Button) rootView.findViewById(R.id.edgeRoundedButton);
        self.edgeRoundedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                for (VDShapeBrush brush : self.shapeBrushes) {
                    brush.setEdgeRounded(v.isSelected());
                }
            }
        });

        self.oneStrokeOneLayerButton = (Button) rootView.findViewById(R.id.oneStrokeOneLayerButton);
        self.oneStrokeOneLayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                self.penBrush.setOneStrokeToLayer(v.isSelected());
                for (VDDrawingBrush brush : self.shapeBrushes) {
                    brush.setOneStrokeToLayer(v.isSelected());
                }
            }
        });

        self.singleSelectionButtons = new ArrayList<>();
        self.singleSelectionButtons.add(self.penButton);
        self.singleSelectionButtons.add(self.shapeButton);
        self.singleSelectionButtons.add(self.textButton);

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
                    self.penBrush.setSize(thickness);
                    self.textBrush.setSize(thickness);
                    for (VDDrawingBrush brush : self.shapeBrushes) {
                        brush.setSize(thickness);
                    }
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