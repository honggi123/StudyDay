package com.coworkerteam.coworker.data.model.custom
import android.util.Log

import android.content.Context
import android.graphics.Canvas
import android.widget.TextView
import com.coworkerteam.coworker.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class StatisticsMarkerView : MarkerView {

    private lateinit var tvContent: TextView
    private lateinit var texts: ArrayList<String>

    // marker
    constructor(context: Context?, layoutResource: Int, texts: ArrayList<String>) : super(
        context,
        layoutResource
    ) {
        tvContent = findViewById(R.id.tvContent)
        this.texts = texts
    }

    // draw override를 사용해 marker의 위치 조정 (bar의 상단 중앙)
    override fun draw(canvas: Canvas?) {
        canvas!!.translate(-(width / 2).toFloat(), -(height/2).toFloat())
        super.draw(canvas)
    }

    // entry를 content의 텍스트에 지정
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val index = e!!.x!!.toInt()
        tvContent.text = texts.get(index)
        Log.d("StatisticsMarkerView",texts.get(index))
        super.refreshContent(e, highlight)
    }

}