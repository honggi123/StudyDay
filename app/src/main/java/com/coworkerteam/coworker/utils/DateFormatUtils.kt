package com.coworkerteam.coworker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormatUtils {


    fun getTodayDate() : String{
        var now = System.currentTimeMillis()
        var mDate = Date(now)

        var cdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        return cdf.format(mDate)
    }

    // 너의 하루는 감정글 날짜 표시 형식
    fun daysToStringformat(end:String, start:String) : String{
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

    // 너의 하루는 공부인증 글 날짜 표시 형식
    fun daysToStringformat_successPost(date:String) : String{
        var cdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        var cdf2  = SimpleDateFormat("M월 d일", Locale.KOREAN)
        var date2 = cdf.parse(date)
        return cdf2.format(date2)
    }

    fun secondsToHourMin(sec : Int) : String{
        if (sec<60*60){
            return (sec/60).toString() + "분"
        }else{
            var hour = (sec/(60*60))
            var min = (sec/60) - (hour * 60)
            return hour.toString() + " 시간 " + min.toString() +" 분"
        }
    }


}