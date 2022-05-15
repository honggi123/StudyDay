package com.coworkerteam.coworker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getTodayDate() : String{
        var now = System.currentTimeMillis()
        var mDate = Date(now)

        var cdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        return cdf.format(mDate)
    }


    // 너의 하루는 날짜 표시 형식
    fun daysToformat_Yourday(end:String, start:String) : String{
        var cdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        var startDate = cdf.parse(start)
        var endDate = cdf.parse(end)

        val diff = endDate.time - startDate.time

        when(diff.toInt()){
            0 -> return "오늘"
            in 1..365 -> return (diff/30).toString() + "일전"
            else -> return (diff/365).toString() + "년전"
        }
    }


}