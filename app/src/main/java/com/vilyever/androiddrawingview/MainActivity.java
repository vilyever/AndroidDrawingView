package com.vilyever.androiddrawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;

public class MainActivity extends AppCompatActivity {

    private DrawingFragment drawingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        VDContextHolder.initial(getApplicationContext());

        getFragmentManager().beginTransaction()
                .replace(R.id.drawingFragmentLayout, getDrawingFragment(), "drawingFragment")
                .commit();

        this.testLayout = (RelativeLayout) findViewById(R.id.testLayout);
        TestView testView = new TestView(this);
        testView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.testLayout.addView(testView);
    }
    private RelativeLayout testLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public DrawingFragment getDrawingFragment() {
        if (drawingFragment == null) {
            drawingFragment = (DrawingFragment) getFragmentManager().findFragmentByTag("drawingFragment");
            if (drawingFragment == null) {
                drawingFragment = new DrawingFragment();
            }
        }
        return drawingFragment;
    }

    public static class TestView extends RelativeLayout {


        public TestView(Context context) {
            super(context);
        }

        public TestView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);

            RectF rect = new RectF(300, 300, 800, 800);
            float size = 20;
            RectF innerRect = new RectF(rect.left + size * 2, rect.top + size, rect.right - size * 2, rect.bottom - size);

            Path path = new Path();
            path.moveTo(rect.left, rect.top);
            path.lineTo(rect.right, rect.top);
            path.lineTo(rect.left, rect.bottom);
            path.lineTo(rect.right, rect.bottom);
            path.lineTo(rect.left, rect.top);

            path.lineTo(innerRect.left, innerRect.top);
            path.lineTo(innerRect.right, innerRect.bottom);
            path.lineTo(innerRect.left, innerRect.bottom);
            path.lineTo(innerRect.right, innerRect.top);
            path.lineTo(innerRect.left, innerRect.top);

            path.lineTo(rect.left, rect.top);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            paint.setColor(Color.RED);

            canvas.drawPath(path, paint);
        }
    }
}
