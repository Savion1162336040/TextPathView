package yanzhikai.textpath;

import android.content.Context;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import yanzhikai.textpath.painter.SyncPathPainter;

/**
 * author : yany
 * e-mail : yanzhikai_yjk@qq.com
 * time   : 2018/03/13
 * desc   : 所有路径按顺序绘画
 */

public class SyncPathView extends PathView {
    //画笔特效
    private SyncPathPainter mPainter;

    //路径长度总数
    private float mLengthSum = 0;

    public SyncPathView(Context context) {
        super(context);
        init();
    }

    public SyncPathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SyncPathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void drawPath(float progress) {
        drawPath(0, progress);
    }

    @Override
    public void drawPath(float start, float stop) {
        mStart = validateProgress(start);
        mStop = validateProgress(stop);

        mStartValue = mLengthSum * mStart;
        mStopValue = mLengthSum * mStop;

        checkFill(stop);

        //重置路径
        mPathMeasure.setPath(mPath, false);
        mDst.reset();
        mPaintPath.reset();

//每个片段的长度
        float segmentLength = mPathMeasure.getLength();
        //是否已经确定起点位置
        boolean findStart = false;
        if (mStopValue <= segmentLength) {
            mPathMeasure.getSegment(mStartValue, mStopValue, mDst, true);
        } else {
            while (mStopValue > segmentLength) {
                mStopValue = mStopValue - segmentLength;
                if (findStart) {
                    //已经确定起点
                    mPathMeasure.getSegment(0, segmentLength, mDst, true);
                } else {
                    if (mStartValue <= segmentLength) {
                        //确定起点操作
                        mPathMeasure.getSegment(mStartValue, segmentLength, mDst, true);
                        findStart = true;
                    } else {
                        //未确定起点
                        mStartValue -= segmentLength;
                    }
                }

                if (!mPathMeasure.nextContour()) {
                    break;
                } else {
                    //获取下一段path长度
                    segmentLength = mPathMeasure.getLength();
                }
            }
            //已经确认终点
            mPathMeasure.getSegment(0, mStopValue, mDst, true);
        }


        //绘画画笔效果
        if (showPainterActually) {
            mPathMeasure.getPosTan(mStopValue, mCurPos, null);
            drawPaintPath(mCurPos[0], mCurPos[1], mPaintPath);
        }

        //绘画路径
        postInvalidate();
    }

    private void drawPaintPath(float x, float y, Path paintPath) {
        if (mPainter != null) {
            mPainter.onDrawPaintPath(x, y, paintPath);
        }
    }

    @Override
    protected void initPath() throws Exception {
        if (mPath == null) {
            throw new Exception("PathView can't work without setting a path!");
        }
        mDst.reset();


        mPathMeasure.setPath(mPath, false);
        mLengthSum = mPathMeasure.getLength();
        //获取所有路径的总长度
        while (mPathMeasure.nextContour()) {
            mLengthSum += mPathMeasure.getLength();
        }
    }

    protected void init() {

        //初始化画笔
        initPaint();

        //初始化路径
//        initPath();

    }

    @Override
    public void startAnimation(float start, float end, int animationStyle, int repeatCount) {
        super.startAnimation(start, end, animationStyle, repeatCount);
        if (mPainter != null) {
            mPainter.onStartAnimation();
        }
    }

    //设置画笔特效
    public void setPathPainter(SyncPathPainter painter) {
        this.mPainter = painter;
    }


}
