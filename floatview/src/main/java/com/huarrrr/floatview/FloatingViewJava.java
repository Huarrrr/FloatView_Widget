package com.huarrrr.floatview;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

/**
 * 这是一个可拖动的view，可用于悬浮窗，无需上层应用权限
 * 常驻WebviewActivity 重新加载webview
 *
 * @author Huar 2022.1.14
 */
@SuppressLint("ViewConstructor")
public class FloatingViewJava extends LinearLayoutCompat {
    private final static String TAG = "FloatingView";

    private final Context mContext;
    private int mFloatBallParamsX = 0;  //view 位置初始x

    private int mFloatBallParamsY = 300; // view  位置初始y
    private int inputStartX = 0;

    private int inputStartY = 0;
    private int viewStartX = 0;

    private int viewStartY = 0;

    private WindowManager.LayoutParams mFloatBallParams;

    private WindowManager mWindowManager;
    private ValueAnimator mValueAnimator;
    private int mScreenHeight;

    private int mScreenWidth;
    private int mDpWidth;

    private boolean mIsShow = false;
    private boolean moveVertical = false;
    private boolean isDrag;
    private int slop;

    public boolean isShow() {
        return mIsShow;
    }

    private final onClickAction clickAction;

    public FloatingViewJava(@NonNull Context context, onClickAction onClickAction) {
        super(context);
        this.mContext = context;
        this.clickAction = onClickAction;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_floating_view, this);
        init();
    }

    private void init() {
        initFloatViewParams(mContext);
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        mDpWidth = (int) dp2px(30f);
        slop = 3;
    }

    //view点击回调
    public interface onClickAction {
        void onClick();
    }

    /**
     * 显示悬浮
     */
    public void showFloat() {
        mIsShow = true;
        if (mFloatBallParamsX == -1 || mFloatBallParamsY == -1) {
            //首次打开时，初始化的位置
            mFloatBallParams.x = mScreenWidth - mDpWidth;
            mFloatBallParams.y = mScreenHeight - mDpWidth * 2;
            mFloatBallParamsX = mFloatBallParams.x;
            mFloatBallParamsY = mFloatBallParams.y;
        } else {
            mFloatBallParams.x = mFloatBallParamsX;
            mFloatBallParams.y = mFloatBallParamsY;
        }
        mWindowManager.addView(this, mFloatBallParams);
        //吸附贴边计算和动画
        welt();
    }


    /**
     * 关闭
     */
    public void dismissFloatView() {
        mIsShow = false;
        mWindowManager.removeViewImmediate(this);
    }


    //拖动操作
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.e("----", "按下" + mFloatBallParamsX + "-----" + mFloatBallParamsY);
                if (null != mValueAnimator && mValueAnimator.isRunning()) {
                    mValueAnimator.cancel();
                }
                setPressed(true);
                isDrag = false;
                inputStartX = (int) event.getRawX();
                inputStartY = (int) event.getRawY();
                viewStartX = mFloatBallParams.x;
                viewStartY = mFloatBallParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                int inMovingX = (int) event.getRawX();
                int inMovingY = (int) event.getRawY();
                int MoveX = viewStartX + inMovingX - inputStartX;
                int MoveY = viewStartY + inMovingY - inputStartY;
                if (mScreenHeight <= 0 || mScreenWidth <= 0) {
                    isDrag = false;
                }
                if (Math.abs(inMovingX - inputStartX) > slop && Math.abs(inMovingY - inputStartY) > slop
                ) {
                    isDrag = true;
                    mFloatBallParams.x = MoveX;
                    mFloatBallParams.y = MoveY;
                    updateWindowManager();
                } else {
                    isDrag = false;
                }
                break;

            case MotionEvent.ACTION_UP:
//                Log.e("----", "抬起" + mFloatBallParamsX + "-----" + mFloatBallParamsY);
                if (isDrag) {
                    //恢复按压效果
                    setPressed(false);
                } else {
                    //点击操作
                    if (clickAction != null) {
                        if (isFastClick()) {
                            clickAction.onClick();
                        }
                    }
                }
                //吸附贴边计算和动画
                welt();
                break;
            default:
                break;
        }
        return isDrag || super.onTouchEvent(event);
    }

    /**
     * 贴边逻辑
     */
    private void welt() {
        int movedX = mFloatBallParams.x;
        int movedY = mFloatBallParams.y;
        moveVertical = false;
        if (mFloatBallParams.y < getHeight() && mFloatBallParams.x >= slop && mFloatBallParams.x <= mScreenWidth - getWidth() - slop) {
            movedY = 0;
        } else if (mFloatBallParams.y > mScreenHeight - getHeight() * 2 && mFloatBallParams.x >= slop && mFloatBallParams.x <= mScreenWidth - getWidth() - slop) {
            movedY = mScreenHeight - getHeight();
        } else {
            moveVertical = true;
            movedX = (mFloatBallParams.x < mScreenWidth / 2 - getWidth() / 2) ? 0 : mScreenWidth - getWidth();
        }
        int duration;
        if (moveVertical) {
            mValueAnimator = ValueAnimator.ofInt(mFloatBallParams.x, movedX);
            duration = movedX - mFloatBallParams.x;
        } else {
            mValueAnimator = ValueAnimator.ofInt(mFloatBallParams.y, movedY);
            duration = movedY - mFloatBallParams.y;
        }
        mValueAnimator.setDuration(Math.abs(duration));
        mValueAnimator.addUpdateListener(animation -> {
            int level = (int) animation.getAnimatedValue();
            if (moveVertical) {
                mFloatBallParams.x = level;
            } else {
                mFloatBallParams.y = level;
            }
            updateWindowManager();
        });

        mValueAnimator.setInterpolator(new AccelerateInterpolator());
        mValueAnimator.start();
    }

    /**
     * 更新保存位置
     */
    private void updateWindowManager() {
        mWindowManager.updateViewLayout(this, mFloatBallParams);
        mFloatBallParamsX = mFloatBallParams.x;
        mFloatBallParamsY = mFloatBallParams.y;
    }


    @Override
    protected void onDetachedFromWindow() {
        //进入下个页面的时候贴边动画暂停，
        // 下个页面attached时候会继续动画，
        // 你手速快的话还能在中途接住球继续拖动
        if (null != mValueAnimator && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    /**
     * 横竖切屏后调用，否则贴边计算异常
     *
     * @param orientation
     */
    public void setScreenOrientation(int orientation) {
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ) {
            mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        } else {
            mScreenWidth = mContext.getResources().getDisplayMetrics().heightPixels;
            mScreenHeight = mContext.getResources().getDisplayMetrics().widthPixels;
        }
    }

    /**
     * 获取view参数
     *
     * @param mContext
     */
    private void initFloatViewParams(Context mContext) {
        mFloatBallParams = new WindowManager.LayoutParams();
        mFloatBallParams.flags = mFloatBallParams.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE //避免悬浮球被通知栏部分遮挡
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        mFloatBallParams.dimAmount = 0.2f;

//      mFloatBallParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mFloatBallParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatBallParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatBallParams.gravity = Gravity.START | Gravity.TOP;
        mFloatBallParams.format = PixelFormat.RGBA_8888;
        // 设置整个窗口的透明度
        mFloatBallParams.alpha = 1.0f;
        // 显示悬浮球在屏幕左上角
        mFloatBallParams.x = 0;
        mFloatBallParams.y = 0;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }


    private float dp2px(Float dp) {
        Float scale = mContext.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static final Long MIN_CLICK_DELAY_TIME = 500L;
    public static Long lastClickTime = 0L;

    public static boolean isFastClick() {
        boolean flag = false;
        Long curClickTime = System.currentTimeMillis();
        if (curClickTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

}
