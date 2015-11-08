package com.caijia.creditview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by cai.jia on 2015/11/6.
 */
public class CreditView extends View {

    /**
     * 控件默认的大小
     */
    private static final int DEFAULT_SIZE_DIP = 300;

    /**
     * 控件的半径
     */
    private int mRadius;

    /**
     * 指示器的图标
     */
    private Bitmap mIndicatorIcon;

    /**
     * 屏幕密度
     */
    private float mDensity;

    public CreditView(Context context) {
        super(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CreditView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public CreditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CreditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDensity = getResources().getDisplayMetrics().density;
        mIndicatorIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_credit_icon);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = (int) dpConvertPx(DEFAULT_SIZE_DIP);
        setMeasuredDimension(resolveSize(size, widthMeasureSpec), resolveSize(size, heightMeasureSpec));
    }

    private RectF mOutBounds = new RectF();

    private RectF mInBounds = new RectF();

    private RectF mIndOutBounds = new RectF();

    private RectF mIndInBounds = new RectF();

    private float mStartAngle = 150f;

    private float mTotalAngle = 240f;

    private int mTotal = 100;

    private int mProgress = 0;

    private int mValue;

    public void setProgress(int progress) {
        this.mValue = progress;
    }

    public void start() {
        handler.postDelayed(runnable, 50);
    }

    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mProgress < mValue) {
                ++mProgress;
                invalidate();
                handler.postDelayed(this, 50);
            }
        }
    };

    Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint mCenterTextP = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint mIndicatorP = new Paint(Paint.ANTI_ALIAS_FLAG);

    private SweepGradient mSweepGradient;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Matrix matrix;

    private Rect mTextBounds = new Rect();

    /**
     * 建立一个弧线的path
     */
    private Path buildArcPath(RectF out, RectF in, float startAngle, int value) {
        float perAngle = mTotalAngle / mTotal;
        float sweepAngle = perAngle * value;
        return buildArcPath(out, in, startAngle, sweepAngle);
    }

    private Path buildArcPath(RectF out, RectF in, float startAngle, float sweepAngle) {
        Path path = new Path();
        float degree = 360 - startAngle;
        double cos = Math.cos(Math.toRadians(degree));
        double sin = Math.sin(Math.toRadians(degree));
        float startPx = (float) (mRadius + mRadius * cos);
        float startPy = (float) (mRadius - mRadius * sin);

        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(startPx, startPy);
        path.arcTo(out, mStartAngle, sweepAngle, false);
        path.arcTo(in, (mStartAngle + sweepAngle) % 360, -sweepAngle, false);
        path.close();
        return path;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        mRadius = width / 2;
        mOutBounds.set(0, 0, width, height);

        int cx = width / 2;
        int cy = height / 2;

        mInBounds.set(mOutBounds);
        mInBounds.inset(dpConvertPx(20), dpConvertPx(20));
        Path path = buildArcPath(mOutBounds, mInBounds, mStartAngle, mTotal);

        //画一个最外边带渐变的弧线
        if (mSweepGradient == null) {
//            mSweepGradient = new SweepGradient(cx, cy,
//                    new int[]{Color.RED,Color.GREEN,Color.BLUE,Color.GRAY}, null);
            mSweepGradient = new SweepGradient(cx, cy,
                    new int[]{0xffFF4A38, 0xffFE9901, 0xffCDD412,
                            0xff5cd412, 0xff0CDC8D, 0xff0CDC8D, 0xff0CDC8D}, null);
        }

        if (matrix == null) {
            matrix = new Matrix();
            matrix.postRotate(mStartAngle,cx,cy);
            mSweepGradient.setLocalMatrix(matrix);
        }
        mPaint.setShader(mSweepGradient);
        canvas.drawPath(path, mPaint);

        drawArcLine(canvas);

        //画指示器的线
        mIndOutBounds.set(mOutBounds);
        float offset = mTextBounds.height() + dpConvertPx(24);
        mIndOutBounds.inset(offset, offset);

        mIndInBounds.set(mIndOutBounds);
        mIndInBounds.inset(dpConvertPx(4), dpConvertPx(4));
        Path indPath = buildArcPath(mIndOutBounds, mIndInBounds, mStartAngle, mProgress);

        mIndicatorP.setColor(0xff2C8DF8);
        canvas.drawPath(indPath, mIndicatorP);

        //画指示器
        drawIndicatorIcon(canvas, mStartAngle, mProgress);

        //画控件中间和下面的文字
        drawArcText(canvas, mProgress);
    }

    private void drawIndicatorIcon(Canvas canvas, float startAngle, int value) {
        float perAngle = mTotalAngle / mTotal;
        float sweepAngle = perAngle * value;
        drawIndicatorIcon(canvas, startAngle, sweepAngle);
    }

    private void drawIndicatorIcon(Canvas canvas, float startAngle, float sweepAngle) {
        //270是字体的方向相对于画圆弧的方向(顺时针)是270
        float degree = 270 - startAngle - sweepAngle;
        //360是画圆弧的方向(顺时针)相对于中心圆0度正好是360(也就是说画圆弧360度正好到达中心圆的0度)
        double cos = Math.cos(Math.toRadians(360 - startAngle - sweepAngle));
        double sin = Math.sin(Math.toRadians(360 - startAngle - sweepAngle));

        float radius = mIndInBounds.height() / 2;
        float startPx = (float) (mRadius + radius * cos);
        float startPy = (float) (mRadius - radius * sin);
        int saveCount = canvas.save();
        canvas.translate(startPx, startPy);
        canvas.rotate(-degree);
        Matrix matrix = new Matrix();
        matrix.postTranslate(-mIndicatorIcon.getWidth() / 2, -mIndicatorIcon.getHeight() / 2 - 4 * mDensity);
        matrix.postScale(mDensity / 2, mDensity / 2, 0.5f, 0.5f);
        canvas.drawBitmap(mIndicatorIcon, matrix, mIndicatorP);
        canvas.restoreToCount(saveCount);
    }

    private void drawArcText(Canvas canvas, int value) {
        mCenterTextP.setColor(0xff2C8DF8);
        mCenterTextP.setTextSize(spConvertPx(40));

        //画中间的字
        String cText = String.valueOf(value);
        mCenterTextP.getTextBounds(cText, 0, cText.length(), mTextBounds);
        canvas.drawText(cText, (getWidth() - mTextBounds.width()) / 2,
                getHeight() / 2 + mTextBounds.height() / 3, mCenterTextP);

        String creditText;
        if (value >= 80) {
            creditText = "信用优秀";
        } else if (value >= 60) {
            creditText = "信用良好";
        } else {
            creditText = "信用较差";
        }

        mCenterTextP.setTextSize(spConvertPx(26));
        mCenterTextP.getTextBounds(creditText, 0, creditText.length(), mTextBounds);
        canvas.drawText(creditText, (getWidth() - mTextBounds.width()) / 2,
                getHeight() - mTextBounds.height() / 2, mCenterTextP);
    }

    /**
     * 画圆弧上的刻度
     */
    private void drawArcLine(Canvas canvas) {
        RectF innerBounds = new RectF(mOutBounds);
        innerBounds.inset(dpConvertPx(20), dpConvertPx(20));

        mLinePaint.setColor(Color.WHITE);

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(spConvertPx(14));

        //一个刻度有几度
        int times = 2;
        float perAngle = mTotalAngle / mTotal * times;
        int length = mTotal / times;

        for (int i = 0; i <= length; i++) {
            boolean big = i % 10 == 0;
            mLinePaint.setStrokeWidth(big ? 3 : 1);

            float sweepAngle = perAngle * i;
            float degree = 270 - mStartAngle - sweepAngle;
            double cos = Math.cos(Math.toRadians(360 - mStartAngle - sweepAngle));
            double sin = Math.sin(Math.toRadians(360 - mStartAngle - sweepAngle));

            float radius = innerBounds.height() / 2;

            float ox = (float) (mRadius * (1 + cos));
            float oy = (float) (mRadius * (1 - sin));

            float ix = (float) (mRadius + cos * radius);
            float iy = (float) (mRadius - sin * radius);

            canvas.drawLine(ox, oy, ix, iy, mLinePaint);
            if (big) {
                //画大刻度的文字
                String text = String.valueOf(i * 2);
                mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
                int textWidth = mTextBounds.width();
                int textHeight = mTextBounds.height();

                int saveCount = canvas.save();
                canvas.translate(ix, iy);
                canvas.rotate(-degree);

                //是否是在开始位置或者结束位置
                boolean isStart = i == 0;
                boolean isEnd = i == length;
                if (isStart) {
                    canvas.drawText(text, 0, textHeight, mTextPaint);
                } else if (isEnd) {
                    canvas.drawText(text, -textWidth, textHeight, mTextPaint);
                } else {
                    canvas.drawText(text, -textWidth / 2, textHeight, mTextPaint);
                }
                canvas.restoreToCount(saveCount);
            }
        }
    }

    private float spConvertPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    private float dpConvertPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, getResources().getDisplayMetrics());
    }
}
