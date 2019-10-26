package com.qw.curtain.lib;

import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.qw.curtain.lib.shape.Shape;

/**
 * https://github.com/soulqw/Curtain
 *
 * @author cd5160866
 */
public class Curtain {

    private FragmentActivity activity;

    private SparseArray<HollowInfo> hollows;

    private CallBack callBack;

    private boolean cancelBackPressed = true;

    private int curtainColor = 0xAA000000;

    private int topViewId;

    private int animationStyle = 0;

    public Curtain(Fragment fragment) {
        this(fragment.getActivity());
    }

    public Curtain(FragmentActivity activity) {
        this.activity = activity;
        this.hollows = new SparseArray<>();
    }

    /**
     * @param which 页面上任一要高亮的view
     */
    public Curtain with(View which) {
        return with(which, true);
    }

    /**
     * @param which                     页面上任一要高亮的view
     * @param isAutoAdaptViewBackGround 是否自动适配View背景形状 (不一定完全生效，如果无法满足的话，可自定义形状)
     * @see #withShape(View, Shape)
     */
    public Curtain with(View which, boolean isAutoAdaptViewBackGround) {
        getHollowInfo(which)
                .setAutoAdaptViewBackGround(isAutoAdaptViewBackGround);
        return this;
    }

    /**
     * 指定区域的padding
     *
     * @param which 该view对应的蒙层区域
     */
    public Curtain withPadding(View which, int padding) {
        getHollowInfo(which).padding = padding;
        return this;
    }

    /**
     * 指定蒙层大小
     *
     * @param which  以该View的左上角作为初始坐标
     * @param width  宽
     * @param height 高
     */
    public Curtain withSize(View which, int width, int height) {
        getHollowInfo(which).targetBound = new Rect(0, 0, width, height);
        return this;
    }

    /**
     * 设置蒙层偏移量
     *
     * @param which     view对应产生的蒙层
     * @param offset    偏移量 px
     * @param direction 偏移方向
     */
    public Curtain withOffset(View which, int offset, @HollowInfo.direction int direction) {
        getHollowInfo(which).setOffset(offset, direction);
        return this;
    }

    /**
     * 设置自定义形状
     *
     * @param which 目标view
     * @param shape 形状
     */
    public Curtain withShape(View which, Shape shape) {
        getHollowInfo(which).setShape(shape);
        return this;
    }

    /**
     * 自定义的引导页蒙层和镂空部分View
     */
    public Curtain setTopView(@LayoutRes int layoutId) {
        this.topViewId = layoutId;
        return this;
    }

    /**
     * 设置蒙层背景颜色
     *
     * @param color 颜色
     */
    public Curtain setCurtainColor(int color) {
        this.curtainColor = color;
        return this;
    }

    public Curtain setCurtainColorRes(@ColorRes int color) {
        this.curtainColor = color;
        return this;
    }

    /**
     * 是否允许回退关闭蒙层
     *
     * @param cancelBackPress 是否允许回退关闭蒙层
     */
    public Curtain setCancelBackPressed(boolean cancelBackPress) {
        this.cancelBackPressed = cancelBackPress;
        return this;
    }

    /**
     * 设置蒙层展示回调回调
     *
     * @param callBack 如果你需要监听的话
     */
    public Curtain setCallBack(CallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    /**
     * 设置蒙层出现的动画 默认渐隐
     *
     * @param animation 动画style
     */
    public Curtain setAnimationStyle(@StyleRes int animation) {
        this.animationStyle = animation;
        return this;
    }

    public void show() {
        if (hollows.size() == 0) {
            throw new IllegalStateException("with out any views");
        }
        View checkStatusView = hollows.valueAt(0).targetView;
        if (checkStatusView.getWidth() == 0) {
            checkStatusView.post(new Runnable() {
                @Override
                public void run() {
                    show();
                }
            });
            return;
        }
        GuideDialogFragment guider = new GuideDialogFragment();
        guider.setCancelable(cancelBackPressed);
        guider.setCallBack(callBack);
        guider.setAnimationStyle(animationStyle);
        guider.setTopViewRes(topViewId);
        GuideView guideView = new GuideView(activity);
        guideView.setCurtainColor(curtainColor);
        addHollows(guideView);
        guider.show(guideView);
    }

    private HollowInfo getHollowInfo(View which) {
        HollowInfo info = hollows.get(which.hashCode());
        if (null == info) {
            info = new HollowInfo(which);
            info.targetView = which;
            hollows.append(which.hashCode(), info);
        }
        return info;
    }

    private void addHollows(GuideView guideView) {
        HollowInfo[] tobeDraw = new HollowInfo[hollows.size()];
        for (int i = 0; i < hollows.size(); i++) {
            tobeDraw[i] = hollows.valueAt(i);
        }
        guideView.setHollowInfo(tobeDraw);
    }

    public static class ViewGetter {

        /**
         * 获取AdapterView 如(ListView,GridView 等中的ChildItem)
         *
         * @param targetContainer such as ListView
         * @param position        你需要的位置
         * @return ItemView
         * @see android.widget.ListView
         * @see android.widget.GridView
         */
        @Nullable
        @CheckResult
        public static View getFromAdapterView(AdapterView targetContainer, int position) {
            View view;
            try {
                view = targetContainer.getChildAt(position - targetContainer.getFirstVisiblePosition());
            } catch (Exception e) {
                return null;
            }
            return view;
        }
    }

    public interface CallBack {

        /**
         * 展示成功
         */
        void onShow(IGuide iGuide);

        /**
         * 消失
         */
        void onDismiss(IGuide iGuide);

    }
}