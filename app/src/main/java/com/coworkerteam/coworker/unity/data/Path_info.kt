package com.coworkerteam.coworker.unity.data

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

class Path_info {

    // 사용자 닉네임
    var name : String? = null

    // 안드로이드에서 그릴때 사용하는 객체
    var paint : Paint
    var path : Path

    // 펜 타입 1. 펜 2. 형광펜 3. 붓 4.지우개
    var pentype : Int = 1
    var penmode = false
    var listxy : ArrayList<Xy>  = ArrayList<Xy>()

    // 도형 타입 1. 원 2. 삼각형 3. 사각형
    var shapemode = false
    var shapetype = 1
    var shapeRightX = 0f    // 오른쪽 하단 x 좌표값
    var shapeRightY = 0f    // 오른쪽 하단 y 좌표값

    // 지우개 크기
    var erasestrokewidth = 50f

    // 펜 색상, 펜 굵기
    var pencolor : String = ""
    var penwidth : Float  = 50f

    init {
        paint = Paint()
        path = Path()
    }

    fun setpaint(p : Paint){
        this.paint = p
    }

    fun setpath(p : Path){
        this.path = p
    }

    fun setxy(x: Float, y: Float){
        listxy.add(Xy(x,y))
    }


    fun setname(name : String){
        this.name = name
    }


    fun setpencolor(color : Int){
        pencolor = java.lang.String.format("#%06X", 0xFFFFFF and color)
    }

    fun setpenwidth(width : Float){
        penwidth = width
    }


    fun setshapetype(type: Int){
        this.shapetype = type
        paint.color = Color.BLACK
        paint.strokeWidth = 2f

        paint.setStyle(Paint.Style.STROKE)
        paint.setAntiAlias(true)
        paint.setDither(true)

        shapemode = true
        penmode = false
    }


    fun setpentype(type: Int){
        this.pentype = type
        penmode = true
        shapemode = false

        when(type){
            1 -> {
                paint.strokeMiter = 100f
                paint.setAntiAlias(false)
            }
            2-> {           // 형광펜
                paint.alpha = 125
            }
            3 -> {            // 붓
                paint.alpha = 25
            }
            4->{        // 지우개 모드
                paint.color = Color.WHITE
                pencolor = java.lang.String.format("#%06X", 0xFFFFFF and Color.WHITE)
                paint.strokeWidth = erasestrokewidth
            }
        }
    }

}

