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
  compile 'com.github.vilyever:AndroidDrawingView:1.6.6'
}
```

## Usage
```java

<com.vilyever.drawingview.DrawingView
    android:id="@+id/drawingView"
    android:layout_width="0dp"
    android:layout_height="match_parent"
    android:layout_weight="1"/>

drawingView = (DrawingView) rootView.findViewById(R.id.drawingView);
drawingView.setUndoRedoStateDelegate(new DrawingView.UndoRedoStateDelegate() {
    @Override
    public void onUndoRedoStateChange(DrawingView drawingView, boolean canUndo, boolean canRedo) {
        undoButton.setEnabled(canUndo);
        redoButton.setEnabled(canRedo);
    }
});

drawingView.setInterceptTouchDelegate(new DrawingView.InterceptTouchDelegate() {
    @Override
    public void requireInterceptTouchEvent(DrawingView drawingView, boolean isIntercept) {

    }
});

drawingView.setBackgroundDatasource(new DrawingView.BackgroundDatasource() {
    @Override
    public Drawable gainBackground(DrawingView drawingView, String identifier) {
        return null;
    }
});

penBrush = PenBrush.defaultBrush();
drawingView.setBrush(penBrush);
```

## License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

