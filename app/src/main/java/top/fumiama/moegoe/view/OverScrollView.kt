package top.fumiama.moegoe.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ScrollView


class OverScrollView: ScrollView {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    var maxOverScrollY = 0
    private fun scrollDelta(deltaY: Int){
        translationY -= deltaY
    }

    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        Log.d("MyOVS", "dY: $deltaY, isT: $isTouchEvent")
        if(isTouchEvent) scrollDelta(deltaY)
        return super.overScrollBy(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            maxOverScrollX,
            maxOverScrollY,
            isTouchEvent
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if(scrollY == getChildAt(0).height - height || translationY != 0f) ev?.let {
            when(it.action){
                MotionEvent.ACTION_UP ->{
                    if(-translationY <= maxOverScrollY / 2) downToNormal()
                    else upOverScroll()
                }
                else -> {}
            }
        }
        return super.onTouchEvent(ev)
    }

    fun upOverScroll(){
        ObjectAnimator.ofFloat(this, "translationY", translationY, -maxOverScrollY.toFloat()).setDuration(233).start()
        fullScroll(FOCUS_DOWN)
    }

    fun downToNormal(){
        ObjectAnimator.ofFloat(this, "translationY", translationY, 0f).setDuration(233).start()
    }
}