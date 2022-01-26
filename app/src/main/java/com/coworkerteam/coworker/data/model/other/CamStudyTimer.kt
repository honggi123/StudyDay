package com.coworkerteam.coworker.data.model.other

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*
import kotlin.math.roundToInt

class CamStudyTimer(private var textView: TextView, private var statusImage: ImageView) {
    val TAG = "CamStudyTimer"

    lateinit var timer: Timer //시간을 재주는 타이머 객체 - 공부 타이머, 휴식시간 타이머 두가지의 경우의 타이머가 초기화 됨
    var timerStratTime: Double = 0.0    //공부 시작전 기본으로 이미 세팅된 시작 시간
    var timerStudyTime: Double = 0.0  //현재 공부 시간
    var timerRestTime: Double = 0.0     //휴식한 시간
    var isStudyTimer: Boolean? = null   //공부 타이머가 시작중인지 판단하는 함수

    fun init(startTime: Double) {
        timerStratTime = startTime
        timerStudyTime = startTime
        setTextTime(timerStratTime)
    }

    //Host에 대한 함수, 공부 시작하는 타이머 셋팅
    fun startStudyTimer() {
        //공부 중이라는 판단 변수 값 변경
        isStudyTimer = true

        //휴식시간 타이머를 재고 있을 경우, 그 타이머를 멈춰야한다.
        if (::timer.isInitialized) {
            timer.cancel()
        }

        //타이머는 동일한 객체를 여러번 사용 못하기에, 다시 사용하려면 다시 초기화 해야한다.
        timer = Timer()

        //타이머 객체 시작 시간 설정
        if (timerStudyTime > 0.0) {
            //만약 스터디방에서 공부하던 기존 시간이 있다면 현재 스터디방 공부 기존시간을 타이머 시작시간으로 실행
            val time = timerStudyTime
            timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        } else {
            //만약 스터디방에서 공부하던 기존 시간이 없다면, API에서 받아온 시작 시간으로 타이머 시작
            val time = timerStratTime
            timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        }

        statusImage.isSelected = true
    }

    //Host에 대한 함수, 쉬기 시작하는 타이머 세팅
    fun startRestTimer() {
        //공부를 진행 중이였을 때만, 쉬는 타이머로 전환 가능
        if (isStudyTimer == true) {
            //공부 중이라는 판단 변수 값 변경
            isStudyTimer = false

            //기존 공부시간 재던 타이머 멈춤
            if (::timer.isInitialized) {
                timer.cancel()
            }

            //쉬는시간 잴 타이머 세팅
            timer = Timer()
            timer.scheduleAtFixedRate(TimeTask(timerRestTime), 0, 1000)
        }

        statusImage.isSelected = false
    }

    //호스트가 아닌 다른 타인의 타이머를 변경상태를 적용시키는 함수
    fun setOtherTimer(status: String) {
        //타이머를 실행중인 상태로 변경
        if (status.equals("run")) {
            //공부 중이라는 판단 변수 값 변경
            isStudyTimer = true

            //공부 시작 타이머 셋팅
            timer = Timer()
            val time = timerStudyTime
            timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)

            statusImage.isSelected = true

            //타이머를 일시 중지 상태로 변경
        } else if (status.equals("pause")) {
            //공부 중이라는 판단 변수 값 변경
            isStudyTimer = false

            //실행중이던 타이머가 있을 경우, 그 타이머 종료
            if (::timer.isInitialized) {
                timer.cancel()
            }

            statusImage.isSelected = false
        }
    }

    fun endTimer() {
        //실행중이던 타이머가 있을 경우, 그 타이머 종료
        if (::timer.isInitialized) {
            timer.cancel()
        }
    }

    //현재 타이머 상태 반환해주는 함수
    fun getTimerStatus(): String {
        if (isStudyTimer == true) {
            return "run"
        } else {
            return "pause"
        }
    }

    fun setTextTime(time: Double) {
        CoroutineScope(Dispatchers.Main).async {
            textView.text = getTimeStringFromDouble(time)
        }
    }

    //타이머 시간, UI에 출력할 형식으로 변경
    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d:%02d", hour, min, sec)

    //타이머 Task
    private inner class TimeTask(private var time: Double) : TimerTask() {
        override fun run() {
            time++
            Log.d(TAG, time.toString())

            if (isStudyTimer!!) {
                setTextTime(time)
                timerStudyTime = time
            } else {
                timerRestTime = time
            }

        }
    }

}