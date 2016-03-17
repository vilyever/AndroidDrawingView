package com.vilyever.androiddrawingview;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vilyever.drawingview.DrawingView;
import com.vilyever.drawingview.brush.drawing.CenterCircleBrush;
import com.vilyever.drawingview.brush.drawing.CircleBrush;
import com.vilyever.drawingview.brush.drawing.DrawingBrush;
import com.vilyever.drawingview.brush.drawing.PenBrush;
import com.vilyever.drawingview.brush.drawing.EllipseBrush;
import com.vilyever.drawingview.brush.drawing.PolygonBrush;
import com.vilyever.drawingview.brush.drawing.RectangleBrush;
import com.vilyever.drawingview.brush.drawing.IsoscelesTriangleBrush;
import com.vilyever.drawingview.brush.drawing.LineBrush;
import com.vilyever.drawingview.brush.drawing.RhombusBrush;
import com.vilyever.drawingview.brush.drawing.RightAngledTriangleBrush;
import com.vilyever.drawingview.brush.drawing.RoundedRectangleBrush;
import com.vilyever.drawingview.brush.drawing.ShapeBrush;
import com.vilyever.drawingview.brush.text.TextBrush;
import com.vilyever.drawingview.model.DrawingStep;

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

    private DrawingView drawingView;

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
    private Button fillTypeButton;
    private Button edgeRoundedButton;
    private Button oneStrokeOneLayerButton;
    private Button deleteLayerButton;

    private ThicknessAdjustController thicknessAdjustController;

    private List<Button> singleSelectionButtons;

    private List<ShapeBrush> shapeBrushes = new ArrayList<>();
    private PenBrush penBrush;
    private TextBrush textBrush;

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

        self.drawingView = (DrawingView) rootView.findViewById(R.id.drawingView);
        self.drawingView.setUndoRedoStateDelegate(new DrawingView.UndoRedoStateDelegate() {
            @Override
            public void onUndoRedoStateChange(DrawingView drawingView, boolean canUndo, boolean canRedo) {
                self.undoButton.setEnabled(canUndo);
                self.redoButton.setEnabled(canRedo);
            }
        });

        self.drawingView.setInterceptTouchDelegate(new DrawingView.InterceptTouchDelegate() {
            @Override
            public void requireInterceptTouchEvent(DrawingView drawingView, boolean isIntercept) {

            }
        });

        self.drawingView.setDrawingStepDelegate(new DrawingView.DrawingStepDelegate() {
            @Override
            public void onDrawingStepBegin(DrawingView drawingView, DrawingStep step) {
            }

            @Override
            public void onDrawingStepChange(DrawingView drawingView, DrawingStep step) {

            }

            @Override
            public void onDrawingStepEnd(DrawingView drawingView, DrawingStep step) {
            }

            @Override
            public void onDrawingStepCancel(DrawingView drawingView, DrawingStep step) {
            }
        });

        self.drawingView.setBackgroundDatasource(new DrawingView.BackgroundDatasource() {
            @Override
            public Drawable gainBackground(DrawingView drawingView, String identifier) {
                return null;
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

        self.penBrush = PenBrush.defaultBrush();
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

        self.shapeBrushes.add(PolygonBrush.defaultBrush());
        self.shapeBrushes.add(LineBrush.defaultBrush());
        self.shapeBrushes.add(RectangleBrush.defaultBrush());
        self.shapeBrushes.add(RoundedRectangleBrush.defaultBrush());
        self.shapeBrushes.add(CircleBrush.defaultBrush());
        self.shapeBrushes.add(EllipseBrush.defaultBrush());
        self.shapeBrushes.add(RightAngledTriangleBrush.defaultBrush());
        self.shapeBrushes.add(IsoscelesTriangleBrush.defaultBrush());
        self.shapeBrushes.add(RhombusBrush.defaultBrush());
        self.shapeBrushes.add(CenterCircleBrush.defaultBrush());
        self.shapeButton = (Button) rootView.findViewById(R.id.shapeButton);
        self.shapeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawingBrush brush = null;

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
                String name = brush.getClass().getSimpleName();
                name = name.substring(0, name.length() - 5);
                ((Button) v).setText(name);
            }
        });

        self.textBrush = TextBrush.defaultBrush().setTypefaceStyle(Typeface.ITALIC);
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
                int color = Color.argb(Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256, Math.abs(random.nextInt()) % 256);
                ((Button) v).setTextColor(color);
                self.drawingView.setBackgroundColor(0, color);
            }
        });

        self.thicknessButton = (Button) rootView.findViewById(R.id.thicknessButton);
        self.thicknessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.getThicknessAdjustController().setThickness((int) self.penBrush.getSize());
                self.getThicknessAdjustController().popupFromView(v, BasePopupController.PopupDirection.Left, true, 0, 0);
            }
        });

        self.eraserButton = (Button) rootView.findViewById(R.id.eraserButton);
        self.eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                self.penBrush.setIsEraser(v.isSelected());
                for (DrawingBrush brush : self.shapeBrushes) {
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
                for (DrawingBrush brush : self.shapeBrushes) {
                    brush.setColor(color);
                }
            }
        });

        self.fillTypeButton = (Button) rootView.findViewById(R.id.fillTypeButton);
        self.fillTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                ShapeBrush.FillType fillType = v.isSelected() ? ShapeBrush.FillType.Solid : ShapeBrush.FillType.Hollow;
                self.fillTypeButton.setText(fillType.name());
                for (ShapeBrush brush : self.shapeBrushes) {
                    brush.setFillType(fillType);
                }
            }
        });

        self.edgeRoundedButton = (Button) rootView.findViewById(R.id.edgeRoundedButton);
        self.edgeRoundedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                for (ShapeBrush brush : self.shapeBrushes) {
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
                self.textBrush.setOneStrokeToLayer(v.isSelected());
                for (DrawingBrush brush : self.shapeBrushes) {
                    brush.setOneStrokeToLayer(v.isSelected());
                }
            }
        });

        self.deleteLayerButton = (Button) rootView.findViewById(R.id.deleteLayerButton);
        self.deleteLayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) self.drawingView.getLayoutParams();
//                if (small) {
//                    layoutParams.rightMargin = 0;
//                    layoutParams.bottomMargin = 0;
//                }
//                else {
//                    layoutParams.rightMargin = self.drawingView.getWidth() / 2;
//                    layoutParams.bottomMargin = self.drawingView.getHeight() / 2;
//                }
//                self.drawingView.setLayoutParams(layoutParams);
//                small = !small;
                self.drawingView.deleteHandlingLayer();
            }
        });

        self.singleSelectionButtons = new ArrayList<>();
        self.singleSelectionButtons.add(self.penButton);
        self.singleSelectionButtons.add(self.shapeButton);
        self.singleSelectionButtons.add(self.textButton);

        return rootView;
    }
    boolean small = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /* #Accessors */
    public ThicknessAdjustController getThicknessAdjustController() {
        if (self.thicknessAdjustController == null) {
            self.thicknessAdjustController = new ThicknessAdjustController(self.getActivity());
            self.thicknessAdjustController.setThicknessDelegate(new ThicknessAdjustController.ThicknessDelegate() {
                @Override
                public void thicknessDidChangeFromThicknessAdjustController(ThicknessAdjustController controller, int thickness) {
                    self.penBrush.setSize(thickness);
                    self.textBrush.setSize(thickness);
                    for (DrawingBrush brush : self.shapeBrushes) {
                        brush.setSize(thickness);
                    }
                }
            });
        }
        return thicknessAdjustController;
    }

    /* #Private Methods */
    private void selectButton(List<Button> buttons, Button button) {
        for (Button b : buttons) {
            b.setSelected(b == button);
        }
    }
}