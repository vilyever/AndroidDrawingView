package com.vilyever.drawingview.layer;

import android.support.annotation.NonNull;

import com.vilyever.drawingview.brush.VDBrush;
import com.vilyever.drawingview.model.VDDrawingStep;

import java.util.List;

/**
 * VDDrawingLayerViewDelegate
 * AndroidDrawingView <com.vilyever.drawingview>
 * Created by vilyever on 2015/10/30.
 * Feature:
 * 图层基础接口
 *
 * Known Direct implement classes：
 * {@link VDDrawingLayerBaseView}
 * {@link VDDrawingLayerImageView}
 * {@link VDDrawingLayerTextView}
 */
public interface VDDrawingLayerViewProtocol {

    /**
     * 清楚当前所有的绘制
     */
    void clearDrawing();

    /**
     * @param drawingStep
     * @return current step's frame
     */
    /**
     * 在原有绘制基础上绘制传入的step，若传入的step和当前step相同则重绘传入的step
     * @param drawingStep 将要绘制的step
     * @return 绘制step后图层需要的frame，确定图层位置和大小
     */
    VDBrush.Frame appendWithDrawingStep(@NonNull VDDrawingStep drawingStep);

    /**
     * 使用传入的step列表刷新图层
     * @param drawnSteps 绘制于此图层的所有step
     */
    void refreshWithDrawnSteps(@NonNull List<VDDrawingStep> drawnSteps);

    /**
     * 获取当前图层的层级
     * @return 层级
     */
    int getLayerHierarchy();

    /**
     * 当前图层是否正在被操作
     * @return 是否正在被操作
     */
    boolean isHandling();

    /**
     * 设置当前图层是否正在被操作，用于更新图层的显示状态
     * @param handling 是否正在操作
     */
    void setHandling(boolean handling);

    /**
     * 当前图层是否允许被操作，例如base图层是不允许被操作的，也可用于临时禁止图层操作
     * @return 是否允许被操作
     */
    boolean canHandle();

    /**
     * 设置图层是否允许被操作，例如临时禁止图层操作
     * 在图层初始创建的step未完成时，理应禁止图层操作
     * @param canHandle 是否允许被操作
     */
    void setCanHandle(boolean canHandle);
}
