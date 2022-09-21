package com.java.health.care.bed.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.java.health.care.bed.util.DensityUtil.dip2px
import com.java.health.care.bed.util.Utils
import java.util.*

class EcgCM22ShowView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mWidth: Float = 0.toFloat()
    private var mHeight: Float = 0.toFloat()
    private var paint: Paint? = null
    private var path: Path? = null
    private val INTERVAL_SCROLL_REFRESH = 160f

    private var refreshList: MutableList<Float>? = null
    private var showIndex: Int = 0

    private val MAX_VALUE = 600f
    //可以绘制的点数 目前2560*1600 计算为128个
    private var intervalNumHeart: Int = 1
    //间隙 计算得20
    private var intervalRowHeart: Float = 0.toFloat()
    //计算的3.125
    private var intervalColumnHeart: Float = 0.toFloat()
    private val HEART_LINE_STROKE_WIDTH = 4.5f
    private var data: FloatArray? = null
    private var mHeartLinestrokeWidth: Float = 0.toFloat()

    private var row: Int = 0
    private var intervalRow: Float = 0.toFloat()
    private var column: Int = 0
    private var intervalColumn: Float = 0.toFloat()
    private var mGridLinestrokeWidth: Float = 0.toFloat()
    private var mGridstrokeWidthAndHeight: Float = 0.toFloat()

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        path = Path()
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = measuredWidth.toFloat()
        mHeight = measuredHeight.toFloat()

        column = (mWidth / mGridstrokeWidthAndHeight).toInt()
        intervalColumn = mWidth / column
        row = (mHeight / mGridstrokeWidthAndHeight).toInt()
        intervalRow = mHeight / row

        mHeartLinestrokeWidth = dip2px(context, HEART_LINE_STROKE_WIDTH).toFloat()
        setData()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawHeartRefresh(canvas)

    }

    fun showLine(point: Float) {
        if (refreshList == null) {
            refreshList = ArrayList()
            data = FloatArray(intervalNumHeart)
        }
        refreshList!!.add(point)

        postInvalidate()
    }


    private fun drawHeartRefresh(canvas: Canvas) {

        paint!!.reset()
        path!!.reset()
        paint!!.style = Paint.Style.STROKE
        paint!!.color = Color.parseColor("#6EDB75")
        paint!!.strokeWidth = 5f
        paint!!.isAntiAlias = true
        path!!.moveTo(0f, mHeight / 2)

        val nowIndex = if (refreshList == null) 0 else refreshList!!.size
        if (nowIndex == 0) {
            return
        }

        //showIndex 下标始终保持0-127    intervalNumHeart固定为128
        showIndex = if (nowIndex < intervalNumHeart) {
            nowIndex - 1
        } else {
            (nowIndex - 1) % intervalNumHeart
        }


        for (i in 0 until intervalNumHeart) { // 等价于 for (int i = 0 ; i < intervalNumHeart ; i++)
            if (i > refreshList!!.size - 1) {
                break
            }
            if (data == null || data!!.isEmpty() || Utils.isEmpty(refreshList)) {
                break
            }

            if (nowIndex < intervalNumHeart) {
                this.data!![i] = refreshList!![i]
            } else {
                val times = (nowIndex - 1) / intervalNumHeart

                val temp = times * intervalNumHeart + i

                if (temp < nowIndex) {
                    this.data!![i] = refreshList!![temp]
                }
            }
        }

        var nowX: Float
        var nowY: Float
        for (i in data!!.indices) { //遍历数组下标0-data.length
            nowX = i * intervalRowHeart
            var dataValue = data!![i]
            Log.d("aaron====8888999==++", Arrays.toString(data))
//            if (dataValue > 0) {
//                if (dataValue > MAX_VALUE * 0.3f) {
//                    dataValue = MAX_VALUE * 0.3f
//                }
//            } else {
//                if (dataValue < -MAX_VALUE * 0.3f) {
//                    dataValue = -(MAX_VALUE * 0.3f)
//                }
//            }
//            nowY = mHeight / 2 - dataValue * intervalColumnHeart

            //坐标y -0偏置值  最后除以1mv电压
            nowY = (dataValue -128)* intervalColumnHeart*0.2f +mHeight/2

//            Log.d("aaron====8888999=====", nowY.toString())


            if (i - 1 == showIndex) {
                path!!.moveTo(nowX, nowY)

            } else {
                //坐标= mWidth -3*intervalRowHeart 1401-15 2个间隙
                if(nowX>mWidth -3*intervalRowHeart){  //坐标x为最后三个的时候，直接跳出循环，不再绘制。
                    break
                }
                if(nowX==0f){  //坐标x为0的时候，不绘制，只移动
                    path!!.moveTo(nowX, nowY)
                }else{
                    path!!.lineTo(nowX, nowY)

                }
            }

        }

        canvas.drawPath(path!!, paint!!)

    }




    fun setData() {

        intervalRowHeart = mWidth / dip2px(context, INTERVAL_SCROLL_REFRESH)
        intervalNumHeart = (mWidth / intervalRowHeart).toInt()
        intervalColumnHeart = mHeight / (MAX_VALUE * 2)

//        Log.d("aaron==20.0==128==3.125", "$intervalRowHeart====$intervalNumHeart=====$intervalColumnHeart");
    }


}
