package com.ll.viewLibrary.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {
    //存储所有行包含的view
    private List<List<View>> allViews = new ArrayList<>();
    //存储所有行的行高
    private List<Integer> allHeights = new ArrayList<>();

    private final int mHSpace = dp2px(16);//同一行的列间距
    private final int mVSpace = dp2px(8);//行间距

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureView(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutView(changed, l, t, r, b);
    }

    /**
     * 测量View
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void measureView(int widthMeasureSpec, int heightMeasureSpec) {
        //测量过程先测量子view 再测量本身（当然反过来也可以，测量过程是个递归的过程）
        //测量之前先清理相关本地数据，因为onMeasure生命周期可能会走多次
        clearData();
        //获取当前布局容器的padding
        int paddingL = getPaddingLeft();
        int paddingR = getPaddingRight();
        int paddingT = getPaddingTop();
        int paddingB = getPaddingBottom();

        List<View> lineViews = new ArrayList<>();//存储单一行view
        int lineHeight = 0;//行高
        int lineUsedWidth = 0;//当前行占据的宽度

        //当布局控件计算的宽高
        int needWidth = 0;
        int needHeight = 0;

        //获取当前布局更具父布局计算出来的宽高
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);
        int selfWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int selfHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View viewChild = getChildAt(i);
            if (viewChild.getVisibility() == View.GONE) {
                continue;
            }
            LayoutParams layoutParams = viewChild.getLayoutParams();
            int childW = layoutParams.width;
            int childH = layoutParams.height;
            //获得子view的测量模式和大小
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingL + paddingR, childW);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingT + paddingB, childH);
            //测量子view
            viewChild.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            // 布局相关算法
            int childViewMeasuredW = viewChild.getMeasuredWidth();
            int childViewMeasuredH = viewChild.getMeasuredHeight();

            //处理换行
            if (lineUsedWidth + mHSpace + childViewMeasuredW > selfWidth) {
                //存储行数据
                allViews.add(lineViews);
                allHeights.add(lineHeight);

                //处理当前布局的宽高
                needWidth = Math.max(needWidth, lineUsedWidth + mHSpace);
                needHeight = needHeight + lineHeight + mVSpace;

                //clear
                lineViews = new ArrayList<>();
                lineHeight = 0;
                lineUsedWidth = 0;
            }
            //处理正常子view添加逻辑
            lineViews.add(viewChild);
            lineUsedWidth = lineUsedWidth + childViewMeasuredW + mHSpace;
            lineHeight = Math.max(lineHeight, childViewMeasuredH);

            //处理最后一行的问题
            if (i == childCount - 1) {
                //存储行数据
                allViews.add(lineViews);
                allHeights.add(lineHeight);
                //处理当前布局的宽高
                needWidth = Math.max(needHeight, lineUsedWidth + mHSpace);
                needHeight = needHeight + lineHeight;//最后一行不加行间距
            }
        }

        //更具测量模式计算出当前布局控件的宽高
        int widthDimesion = (selfWidthMode == MeasureSpec.EXACTLY ? selfWidth : needWidth) + paddingL + paddingR;
        int heightDimesion = (selfHeightMode == MeasureSpec.EXACTLY ? selfHeight : needHeight) + paddingT + paddingB;
        setMeasuredDimension(widthDimesion, heightDimesion);
    }

    private void clearData(){
        allViews.clear();
        allHeights.clear();
    }
    /**
     * 布局View
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void layoutView(boolean changed, int l, int t, int r, int b) {
        //按照每一行去处理控件view的布局
        int paddingL = getPaddingLeft();
        int paddingT = getPaddingTop();
        List<View> lineView = null;

        for (int i = 0; i < allViews.size(); i++) {
            lineView = allViews.get(i);
            if (lineView == null) {
                continue;
            }
            int lineHeight = allHeights.get(i);
            for (int j = 0; j < lineView.size(); j++){
                View childView = lineView.get(j);
                int childL = paddingL;
                int topFix = 0;
                if (childView.getMeasuredHeight()< lineHeight){
                    topFix = Math.abs(lineHeight - childView.getMeasuredHeight()) / 2; //把不同高度的控件垂直方向居中显示
                }
                int childT = paddingT + topFix;
                int childR = paddingL + childView.getMeasuredWidth();
                int childB = childT + childView.getMeasuredHeight();
                //摆放子view
                childView.layout(childL, childT, childR, childB);

                //同一行的下一个view的左边偏移量
                paddingL = childR + mHSpace;
            }
            //换行处理下一行的上偏移量，同时重置左边偏移量
            paddingL = getPaddingLeft();
            paddingT = paddingT + lineHeight + mVSpace;
        }
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
