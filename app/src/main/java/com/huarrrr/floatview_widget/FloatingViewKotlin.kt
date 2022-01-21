package com.huarrrr.floatview_widget

import android.annotation.SuppressLint
import androidx.appcompat.widget.LinearLayoutCompat
import android.view.WindowManager
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.view.animation.AccelerateInterpolator
import android.content.pm.ActivityInfo
import android.view.Gravity
import android.graphics.PixelFormat
import android.view.LayoutInflater
import kotlin.math.abs

/**
 * 这是一个可拖动的view，可用于悬浮窗，无需上层应用权限
 * 常驻WebviewActivity 重新加载webview
 *
 * @author Huar 2022.1.21
 */
@SuppressLint("ViewConstructor")
class FloatingViewKotlin(private val mContext: Context, private val onClickAction:() -> Unit ) :
    LinearLayoutCompat(
        mContext
    ) {
    private var mFloatBallParamsX = 0 //view 位置初始x
    private var mFloatBallParamsY = 300 // view  位置初始y
    private var inputStartX = 0
    private var inputStartY = 0
    private var viewStartX = 0
    private var viewStartY = 0
    private var mFloatBallParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var mValueAnimator: ValueAnimator? = null
    private var mScreenHeight = 0
    private var mScreenWidth = 0
    private var mDpWidth = 0
    var isShow = false
        private set
    private var moveVertical = false
    private var isDrag = false
    private var slop = 0
    private fun init() {
        initFloatViewParams(mContext)
        mScreenWidth = mContext.resources.displayMetrics.widthPixels
        mScreenHeight = mContext.resources.displayMetrics.heightPixels
        mDpWidth = dp2px(30f).toInt()
        slop = 3
    }

    /**
     * 显示悬浮
     */
    fun showFloat() {
        isShow = true
        if (mFloatBallParamsX == -1 || mFloatBallParamsY == -1) {
            //首次打开时，初始化的位置
            mFloatBallParams!!.x = mScreenWidth - mDpWidth
            mFloatBallParams!!.y = mScreenHeight - mDpWidth * 2
            mFloatBallParamsX = mFloatBallParams!!.x
            mFloatBallParamsY = mFloatBallParams!!.y
        } else {
            mFloatBallParams!!.x = mFloatBallParamsX
            mFloatBallParams!!.y = mFloatBallParamsY
        }
        mWindowManager!!.addView(this, mFloatBallParams)
        //吸附贴边计算和动画
        welt()
    }

    /**
     * 关闭
     */
    fun dismissFloatView() {
        isShow = false
        mWindowManager!!.removeViewImmediate(this)
    }

    //拖动操作
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //Log.e("----", "按下" + mFloatBallParamsX + "-----" + mFloatBallParamsY);
                if (null != mValueAnimator && mValueAnimator!!.isRunning) {
                    mValueAnimator!!.cancel()
                }
                isPressed = true
                isDrag = false
                inputStartX = event.rawX.toInt()
                inputStartY = event.rawY.toInt()
                viewStartX = mFloatBallParams!!.x
                viewStartY = mFloatBallParams!!.y
            }
            MotionEvent.ACTION_MOVE -> {
                val inMovingX = event.rawX.toInt()
                val inMovingY = event.rawY.toInt()
                val moveX = viewStartX + inMovingX - inputStartX
                val moveY = viewStartY + inMovingY - inputStartY
                if (mScreenHeight <= 0 || mScreenWidth <= 0) {
                    isDrag = false
                }
                if (abs(inMovingX - inputStartX) > slop && abs(inMovingY - inputStartY) > slop) {
                    isDrag = true
                    mFloatBallParams!!.x = moveX
                    mFloatBallParams!!.y = moveY
                    updateWindowManager()
                } else {
                    isDrag = false
                }
            }
            MotionEvent.ACTION_UP -> {
                //Log.e("----", "抬起" + mFloatBallParamsX + "-----" + mFloatBallParamsY);
                if (isDrag) {
                    //恢复按压效果
                    isPressed = false
                } else {
                    //点击操作
                    if (isFastClick) {
                        onClickAction.invoke()
                    }
                }
                //吸附贴边计算和动画
                welt()
            }
            else -> {}
        }
        return isDrag || super.onTouchEvent(event)
    }

    /**
     * 贴边逻辑
     */
    private fun welt() {
        var movedX = mFloatBallParams!!.x
        var movedY = mFloatBallParams!!.y
        moveVertical = false
        if (mFloatBallParams!!.y < height && mFloatBallParams!!.x >= slop && mFloatBallParams!!.x <= mScreenWidth - width - slop) {
            movedY = 0
        } else if (mFloatBallParams!!.y > mScreenHeight - height * 2 && mFloatBallParams!!.x >= slop && mFloatBallParams!!.x <= mScreenWidth - width - slop) {
            movedY = mScreenHeight - height
        } else {
            moveVertical = true
            movedX =
                if (mFloatBallParams!!.x < mScreenWidth / 2 - width / 2) 0 else mScreenWidth - width
        }
        val duration: Int
        if (moveVertical) {
            mValueAnimator = ValueAnimator.ofInt(mFloatBallParams!!.x, movedX)
            duration = movedX - mFloatBallParams!!.x
        } else {
            mValueAnimator = ValueAnimator.ofInt(mFloatBallParams!!.y, movedY)
            duration = movedY - mFloatBallParams!!.y
        }
        mValueAnimator!!.setDuration(Math.abs(duration).toLong())
        mValueAnimator!!.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val level = animation.animatedValue as Int
            if (moveVertical) {
                mFloatBallParams!!.x = level
            } else {
                mFloatBallParams!!.y = level
            }
            updateWindowManager()
        })
        mValueAnimator!!.interpolator = AccelerateInterpolator()
        mValueAnimator!!.start()
    }

    /**
     * 更新保存位置
     */
    private fun updateWindowManager() {
        mWindowManager!!.updateViewLayout(this, mFloatBallParams)
        mFloatBallParamsX = mFloatBallParams!!.x
        mFloatBallParamsY = mFloatBallParams!!.y
    }

    override fun onDetachedFromWindow() {
        //进入下个页面的时候贴边动画暂停，
        // 下个页面attached时候会继续动画，
        // 你手速快的话还能在中途接住球继续拖动
        if (null != mValueAnimator && mValueAnimator!!.isRunning) {
            mValueAnimator!!.cancel()
        }
        super.onDetachedFromWindow()
    }

    /**
     * 横竖切屏后调用，否则贴边计算异常
     *
     * @param orientation
     */
    fun setScreenOrientation(orientation: Int) {
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ) {
            mScreenWidth = mContext.resources.displayMetrics.widthPixels
            mScreenHeight = mContext.resources.displayMetrics.heightPixels
        } else {
            mScreenWidth = mContext.resources.displayMetrics.heightPixels
            mScreenHeight = mContext.resources.displayMetrics.widthPixels
        }
    }

    /**
     * 获取view参数
     *
     * @param mContext
     */
    private fun initFloatViewParams(mContext: Context) {
        mFloatBallParams = WindowManager.LayoutParams()
        mFloatBallParams!!.flags =
            (mFloatBallParams!!.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE //避免悬浮球被通知栏部分遮挡
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        mFloatBallParams!!.dimAmount = 0.2f

        mFloatBallParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        mFloatBallParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        mFloatBallParams!!.gravity = Gravity.START or Gravity.TOP
        mFloatBallParams!!.format = PixelFormat.RGBA_8888
        // 设置整个窗口的透明度
        mFloatBallParams!!.alpha = 1.0f
        // 显示悬浮球在屏幕左上角
        mFloatBallParams!!.x = 0
        mFloatBallParams!!.y = 0
        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun dp2px(dp: Float): Float {
        val scale = mContext.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    companion object {
        private const val TAG = "FloatingView"
        private const val MIN_CLICK_DELAY_TIME = 500L
        var lastClickTime = 0L
        val isFastClick: Boolean
            get() {
                var flag = false
                val curClickTime = System.currentTimeMillis()
                if (curClickTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
                    flag = true
                }
                lastClickTime = curClickTime
                return flag
            }
    }

    init {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_floating_view, this)
        init()
    }
}