package com.vilyever.androiddrawingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vilyever.contextholder.VDContextHolder;

public class MainActivity extends AppCompatActivity {

    private DrawingFragment drawingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VDContextHolder.initial(getApplicationContext());

        getFragmentManager().beginTransaction()
                .replace(R.id.drawingFragmentLayout, getDrawingFragment(), "drawingFragment")
                .commit();

        this.testLayout = (RelativeLayout) findViewById(R.id.testLayout);
        this.testView = new TestView(this);
        this.testLayout.addView(this.testView);
        this.testView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Runtime rt=Runtime.getRuntime();
        long maxMemory=rt.maxMemory();
        System.out.println("maxMemory: " + Long.toString(maxMemory / (1024 * 1024)));
    }
    private RelativeLayout testLayout;
    private TestView testView;

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

    public static class TestView extends ImageView {

        public TestView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(20);
//            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);

            Path path = new Path();
//            path.moveTo(200, 200);
//            path.lineTo(800, 800);
            path.addCircle(500, 500, 100, Path.Direction.CCW);
            path.lineTo(800, 800);
            path.lineTo(400, 300);
            path.lineTo(500, 800);


            Region region = new Region(canvas.getClipBounds());

            int x = 400;
            int y = 400;
            int contain = 0;

            for (int i = 0; i <= 200; i++) {
                for (int j = 0; j <= 200; j++) {
                    if (region.contains(x + i, y + j)) {
                        contain++;
                    }
                }
            }

            System.out.println("contain rect " + contain);

            region.setPath(path, region);

            contain = 0;

            for (int i = 0; i <= 200; i++) {
                for (int j = 0; j <= 200; j++) {
                    if (region.contains(x + i, y + j)) {
                        contain++;
                    }
                }
            }

            System.out.println("contain circle " + contain);


            canvas.drawPath(path, paint);
        }
    }
}
