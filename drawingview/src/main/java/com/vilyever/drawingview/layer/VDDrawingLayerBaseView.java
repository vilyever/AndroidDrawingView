package com.vilyever.drawingview.layer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.model.VDDrawingStep;

import java.util.ArrayList;
import java.util.List;

/**
 * VDDrawingLayerBaseView
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/9/24.
 * Feature:
 * base底层view，同步绘制/异步绘制，不可移动变换
 */
public class VDDrawingLayerBaseView extends ImageView implements Runnable, VDDrawingLayerViewProtocol {
    private final VDDrawingLayerBaseView self = this;

    /* #Constructors */
    public VDDrawingLayerBaseView(Context context) {
        super(context);
        self.init();
    }

    /* Public Methods */
    public void drawView(View view) {
        self.checkDrawingBitmap();
        if (self.getDrawingCanvas() != null) {
            self.getDrawingCanvas().save();
            self.getDrawingCanvas().translate(view.getLeft(), view.getTop());

            view.draw(self.getDrawingCanvas());
            self.getDrawingCanvas().restore();

            self.invalidate();
        }
    }

    /* Properties */
    public interface Delegate {
        /**
         * 当前图层busy状态变更时回调
         * {@link #busying}
         * @param baseView 当前图层view
         * @param busying 当前busy状态
         */
        void didChangeBusyState(VDDrawingLayerBaseView baseView, boolean busying);
    }
    private Delegate delegate;
    public VDDrawingLayerBaseView setDelegate(Delegate delegate) {
        this.delegate = delegate;
        return this;
    }
    public Delegate getDelegate() {
        if (delegate == null) {
            delegate = new Delegate() {
                @Override
                public void didChangeBusyState(VDDrawingLayerBaseView baseView, boolean busying) {
                }
            };
        }
        return delegate;
    }

    /**
     * 用于绘制的bitmap，常驻内存
     */
    private Bitmap drawingBitmap;
    private VDDrawingLayerBaseView setDrawingBitmap(Bitmap drawingBitmap) {
        this.drawingBitmap = drawingBitmap;
        return this;
    }
    private Bitmap getDrawingBitmap() {
        return drawingBitmap;
    }

    /**
     * 关联{@link #drawingBitmap}的画布
     */
    private Canvas drawingCanvas;
    private VDDrawingLayerBaseView setDrawingCanvas(Canvas drawingCanvas) {
        this.drawingCanvas = drawingCanvas;
        return this;
    }
    private Canvas getDrawingCanvas() {
        return drawingCanvas;
    }

    /**
     * 在绘制下一step时，临时存储先前绘制状态的bitmap，仅在新绘制时存在，用完即回收
     */
    private Bitmap tempBitmap;
    private VDDrawingLayerBaseView setTempBitmap(Bitmap tempBitmap) {
        this.tempBitmap = tempBitmap;
        return this;
    }
    private Bitmap getTempBitmap() {
        return tempBitmap;
    }

    /**
     * 当前绘制的所有step
     */
    private List<VDDrawingStep> drawnSteps;
    private VDDrawingLayerBaseView setDrawnSteps(List<VDDrawingStep> drawnSteps) {
        this.drawnSteps = drawnSteps;
        return this;
    }
    public List<VDDrawingStep> getDrawnSteps() {
        if (drawnSteps == null) {
            drawnSteps = new ArrayList<>();
        }
        return drawnSteps;
    }

    /**
     * 当前正在绘制的step
     */
    private VDDrawingStep currentDrawingStep;
    private VDDrawingLayerBaseView setCurrentDrawingStep(VDDrawingStep currentDrawingStep) {
        this.currentDrawingStep = currentDrawingStep;
        return this;
    }
    public VDDrawingStep getCurrentDrawingStep() {
        return currentDrawingStep;
    }

    /**
     * 绘制{@link #drawnSteps}时的后台线程
     * 由于绘制{@link #drawnSteps}的复杂度是未知的
     * 为了防止步骤过多或过于复杂引起的UI卡顿，将此操作放到后台进行
     * 而在仅仅绘制新的单一step即{@link #currentDrawingStep}时
     * 对于绘制的实时性要求，将其在UI线程进行同步绘制
     * （即绘制的图形能跟随手指，如果单步绘制过于复杂，导致每一次绘制的时长都会导致卡顿，这样即使将其放到后台绘制，也会引发延时问题，故brush绘制一笔step的复杂时长不宜太长，理应控制在同步绘制不会引起卡顿的范围）
     *
     */
    private Thread drawingThread;
    public VDDrawingLayerBaseView setDrawingThread(Thread drawingThread) {
        this.drawingThread = drawingThread;
        return this;
    }
    public Thread getDrawingThread() {
        return drawingThread;
    }

    /**
     * 当前是否正在后台绘制，是否处于忙碌状态
     */
    private boolean busying;

    /**
     * 使用{@link #uiHandler}来更新状态，无论ui线程或后台线程都可以直接调用此方法
     */
    private VDDrawingLayerBaseView setBusying(boolean busying) {
        this.busying = busying;
        self.getUIHandler().sendEmptyMessage(0);
        return this;
    }
    public boolean isBusying() {
        return busying;
    }

    /**
     * 异步绘制线程{@link #drawingThread}的同步Handler
     * 在异步绘制开始和完成时产生一个回调
     */
    private Handler uiHandler;
    private Handler getUIHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    // 此handler只处理一种信息，无需判断
                    /** {@link VDDrawingLayerBaseView#setBusying(boolean)} */
                    self.getDelegate().didChangeBusyState(self, self.isBusying());
                    return false;
                }
            });
        }
        return uiHandler;
    }

    /* Overrides */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 讲当前已绘制好的bitmap绘制到系统画布上
        canvas.drawBitmap(self.getDrawingBitmap(), 0, 0, null);
    }

    /* Delegates */
    /** {@link Runnable} */
    /**
     * {@link #drawingThread}执行的runnable
     */
    @Override
    public void run() {
//        long beginTime = System.currentTimeMillis();
        try {
            self.setBusying(true); // 开始绘制时进入busying状态

            /**
             * 检查{@link #drawingCanvas}是否存在
             * 由于{@link #drawingBitmap}可能在view尺寸为0时未初始化，与之关联的canvas显然此时也未初始化
             */
            if (self.getDrawingCanvas() != null) {
                // 清空画布
                self.getDrawingCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                /** 绘制需要绘制的步骤step，base图层只处理 {@link VDDrawingStep.StepType#DrawOnBase}，其他type由外部处理 */
                for (int i = 0; i < self.getDrawnSteps().size(); i++) {
                    VDDrawingStep step = self.getDrawnSteps().get(i);
                    if (step.getStepType() == VDDrawingStep.StepType.DrawOnBase) {
                        step.getBrush().drawPath(self.getDrawingCanvas(), step.getDrawingPath(), new VDBrush.DrawingState(VDBrush.DrawingPointerState.ForceFinish));
                    }
                    else if (step.getStepType() == VDDrawingStep.StepType.DrawTextOnBase) {
                        // 拓印text图层到base图层，省去实现与editText相同效果的绘制计算
                        VDDrawingLayerTextView textView = new VDDrawingLayerTextView(self.getContext());
                        textView.appendWithDrawingStep(step);

                        textView.measure(MeasureSpec.makeMeasureSpec(self.getDrawingCanvas().getWidth(), MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(self.getDrawingCanvas().getHeight(), MeasureSpec.UNSPECIFIED));
                        int left = (int) Math.floor(step.getDrawingLayer().getLeft());
                        int top = (int) Math.floor(step.getDrawingLayer().getTop());
                        textView.layout(left, top, left + textView.getMeasuredWidth(), top + textView.getMeasuredHeight());

                        self.getDrawingCanvas().save();
                        self.getDrawingCanvas().translate(step.getDrawingLayer().getLeft(), step.getDrawingLayer().getTop());

                        textView.draw(self.getDrawingCanvas());
                        self.getDrawingCanvas().restore();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
//            while (System.currentTimeMillis() - beginTime < 500) {
//                Thread.yield();
//            }

            // 结束绘制时脱离busying状态
            self.setBusying(false);
            self.postInvalidate();
        }
    }
    /** {@link Runnable} */


    /** {@link VDDrawingLayerViewProtocol} */
    @Override
    public void clearDrawing() {
        self.getDrawnSteps().clear();
        self.setCurrentDrawingStep(null);

        self.checkDrawingBitmap();
        if (self.getDrawingCanvas() != null) {
            self.getDrawingCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        self.invalidate();
    }

    @Override
    public VDBrush.Frame appendWithDrawingStep(@NonNull VDDrawingStep drawingStep) {
        // 禁止在busying时添加step
        if (self.isBusying()) {
            return null;
        }

        /** base图层只处理 {@link VDDrawingStep.StepType#DrawOnBase}，其他type由外部处理 */
        if (drawingStep.getStepType() != VDDrawingStep.StepType.DrawOnBase) {
            return null;
        }

        // 记录当前step为传入step，即使当前step与传入step是同一step也无妨
        self.setCurrentDrawingStep(drawingStep);

        /**
         * step处于very begin状态（即通常是step刚刚创建，此次绘制为该step的第一次绘制）的处理
         * 临时记录当前绘制的bitmap
         * 使用这个状态记录临时bitmap可以明确判断step的开始和结束来释放tempBitmap
         * 而原先在没有状态记录时，每次绘制都存储tempBitmap然后释放会导致频繁的内存操作
         */
        if (drawingStep.getDrawingState().isVeryBegin()) {
            if (self.getDrawingBitmap() != null) {
                self.setTempBitmap(Bitmap.createBitmap(self.getDrawingBitmap(), 0, 0, self.getDrawingBitmap().getWidth(), self.getDrawingBitmap().getHeight()));
            }
        }

        /**
         * step处于very end状态（即通常是step完成时，此次绘制为该step的最后一次绘制之后，即此次调用并不进行绘制）的处理
         * 当前step置空
         * 当前记录的临时tempBitmap释放
         */
        if (drawingStep.getDrawingState().isVeryEnd()) {
            self.setCurrentDrawingStep(null);
            if (self.getTempBitmap() != null && !self.getTempBitmap().isRecycled()) {
                self.getTempBitmap().recycle();
            }
            self.setTempBitmap(null);
            System.gc();
            return null;
        }

        VDBrush.Frame frame = null;

        /**
         * 通常状态的绘制
         * 首先清空画布并绘制存储的tempBitmap
         * 在此基础上绘制当前step
         * 得到绘制当前step所需的frame返回
         * 返回frame主要是在frame为空时提示外层此step无需绘制，没有意义
         * 或者frame需要更多笔画来完成绘制
         */
        self.checkDrawingBitmap();
        if (self.getDrawingCanvas() != null) {
            self.getDrawingCanvas().drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (self.getTempBitmap() != null) {
                self.getDrawingCanvas().drawBitmap(self.getTempBitmap(), 0, 0, null);
            }

            if (drawingStep.getStepType() == VDDrawingStep.StepType.DrawOnBase) {
                frame = self.getCurrentDrawingStep().getBrush().drawPath(self.getDrawingCanvas(), self.getCurrentDrawingStep().getDrawingPath(), drawingStep.getDrawingState());
                drawingStep.getDrawingLayer().setFrame(frame);
            }

            self.invalidate();
        }

        return frame;
    }

    @Override
    public void refreshWithDrawnSteps(@NonNull List<VDDrawingStep> drawnSteps) {
        /**
         * 若当前有后台线程正在绘制上一次请求，打断这个线程
         * 并重新生成一个线程开始绘制当前请求
         */
        try {
            if (self.getDrawingThread() != null) {
                self.getDrawingThread().interrupt();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // 存储step列表
        self.getDrawnSteps().clear();
        self.getDrawnSteps().addAll(drawnSteps);

        self.checkDrawingBitmap();

        self.setDrawingThread(new Thread(self));
        self.getDrawingThread().start();
    }

    @Override
    public int getLayerHierarchy() {
        return 0;
    }

    @Override
    public boolean isHandling() {
        return false;
    }

    @Override
    public void setHandling(boolean handling) {
    }

    @Override
    public boolean canHandle() {
        return false; // base图层不可操作（操作是指平移/旋转/缩放等非绘制操作）
    }

    @Override
    public void setCanHandle(boolean canHandle) {

    }
    /** {@link VDDrawingLayerViewProtocol} */

    /* Private Methods */
    /**
     * 初始化
     */
    private void init() {
        // 在UI线程创建handler，避免懒加载在线程中创建导致错误
        self.getUIHandler();
    }

    /**
     * 检测当前绘制bitmap是否存在
     * 根据当前view状态生成铺满view的bitmap
     */
    private void checkDrawingBitmap() {
        if (self.getWidth() > 0 && self.getHeight() > 0) {
            try {
                if (self.getDrawingBitmap() == null) {
                    self.setDrawingBitmap(Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888));
                    self.setDrawingCanvas(new Canvas(self.getDrawingBitmap()));
                }
                else if (self.getDrawingBitmap().getWidth() != self.getWidth()
                        || self.getDrawingBitmap().getHeight() != self.getHeight()) {
                    self.getDrawingBitmap().recycle();
                    System.gc();
                    self.setDrawingBitmap(Bitmap.createBitmap(self.getWidth(), self.getHeight(), Bitmap.Config.ARGB_8888));
                    self.setDrawingCanvas(new Canvas(self.getDrawingBitmap()));
                }
            }
            catch (Exception e) {
                // in recycler view, the view's size may very large when init
                if (!(e instanceof IllegalArgumentException)) {
                    e.printStackTrace();
                }
            }
        }
    }
}