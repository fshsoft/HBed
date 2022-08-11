package com.java.health.care.bed.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.java.health.care.bed.R;
import com.java.health.care.bed.util.DpUtil;

public class TagValueTextView extends View {

    public TagValueTextView(Context context) {
        super(context);
        init(context);
    }

    public TagValueTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mViewHeight = h;
        mViewWidth = w;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private Context mContext;
    private int mViewWidth;
    private int mViewHeight;
    private int mTextColor;
    private int mValueTextSize = 26;
    private int mTagTextSize = 14;
    private int mMargin = 5;
    private int value = 50;
    private String tag = "分";
    private Paint mValuePaint;
    private Paint mTagPaint;
    private String mValueString;


    /**
     * 设置值
     * @param value
     */
    public void setValue(int value) {
        this.value = value;
        invalidate();
    }

    /**
     * 设置tag
     * @param tag tag
     */
    public void setTag(String tag) {
        this.tag = tag;
        invalidate();
    }

    /**
     * 设置呼吸训练的分数
     * @param mTextColor
     */
    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
        invalidate();
    }

    private void init(Context context) {
        this.mContext = context;
        mTextColor = ContextCompat.getColor(mContext, R.color.color_red_FE647C);
        mValueTextSize = DpUtil.dp2px(mContext,mValueTextSize);
        mTagTextSize = DpUtil.dp2px(mContext,mTagTextSize);
        mMargin = DpUtil.dp2px(mContext,mMargin);

        mValuePaint = new Paint();
        mValuePaint.setTextSize(mValueTextSize);
        mValuePaint.setAntiAlias(true);
        mValuePaint.setColor(mTextColor);

        mTagPaint = new Paint();
        mTagPaint.setAntiAlias(true);
        mTagPaint.setColor(mTextColor);
        mTagPaint.setTextSize(mTagTextSize);
    }

    private Rect valueRect = new Rect();
    private Rect tagRect = new Rect();
    @Override
    protected void onDraw(Canvas canvas) {
        mValuePaint.setColor(mTextColor);
        mTagPaint.setColor(mTextColor);
        mValueString = String.valueOf(value);
        mValuePaint.getTextBounds(mValueString,0,mValueString.length(),valueRect);
        canvas.drawText(mValueString,0,valueRect.height(),mValuePaint);
        mTagPaint.getTextBounds(tag,0,tag.length(),tagRect);
        canvas.drawText(tag,valueRect.width() + mMargin,valueRect.height() - DpUtil.dp2px(mContext,1),mTagPaint);
    }

    //两个参数是父View给的测量建议值MeasureSpec,代码执行到onMeasure(w,h),说明MyCircleView的measure(w,h)在执行中
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mValueString = String.valueOf(value);

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);//宽的测量大小，模式
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);//高的测量大小，模式
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int w = widthSpecSize;   //定义测量宽，高(不包含测量模式),并设置默认值，查看View#getDefaultSize可知
        int h = heightSpecSize;
        mValuePaint.getTextBounds(mValueString,0,mValueString.length(),valueRect);
        mTagPaint.getTextBounds(tag,0,tag.length(),tagRect);
        //处理wrap_content的几种特殊情况
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            w = tagRect.width() + valueRect.width() + mMargin + DpUtil.dp2px(mContext,5);  //单位是px
            h = valueRect.height()+ DpUtil.dp2px(mContext,5);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            //只要宽度布局参数为wrap_content， 宽度给固定值200dp(处理方式不一，按照需求来)
            w = tagRect.width() + valueRect.width() + mMargin + DpUtil.dp2px(mContext,5);
            //按照View处理的方法，查看View#getDefaultSize可知
            h = heightSpecSize;
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            w = widthSpecSize;
            h = valueRect.height()+ DpUtil.dp2px(mContext,5);
        }
        //给两个字段设置值，完成最终测量
        setMeasuredDimension(w, h);
    }


}
