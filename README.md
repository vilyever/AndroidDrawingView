# AndroidDrawingView
绘画板，支持平滑跟随，常用形状，图层变换

## Import
[JitPack](https://jitpack.io/)

Add it in your project's build.gradle at the end of repositories:

```gradle
repositories {
  // ...
  maven { url "https://jitpack.io" }
}
```

Step 2. Add the dependency in the form

```gradle
dependencies {
  compile 'com.github.vilyever:AndroidDrawingView:1.2.1'
}
```

## Usage
```java

<com.vilyever.drawingview.VDDrawingView
        android:id="@+id/drawingView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

drawingView = (VDDrawingView) rootView.findViewById(R.id.drawingView);
drawingView.setDrawingDelegate(new VDDrawingView.DrawingDelegate() {
    @Override
    public void didUpdateCurrentStep(VDDrawingView drawingView, VDDrawingStep step) {
    }

    @Override
    public void didUpdateDrawingData(VDDrawingView drawingView, VDDrawingData data) {
    }

    @Override
    public void didUpdateUndoRedoState(VDDrawingView drawingView, boolean canUndo, boolean canRedo) {
        undoButton.setEnabled(canUndo);
        redoButton.setEnabled(canRedo);
    }

    @Override
    public void didInterceptTouchEvent(VDDrawingView drawingView, boolean isIntercept) {

    }

    @Override
    public Drawable gainBackground(VDDrawingView drawingView, String identifier) {
        return null;
    }
});

penBrush = VDPenBrush.defaultBrush();
drawingView.setBrush(penBrush);
```

## License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

