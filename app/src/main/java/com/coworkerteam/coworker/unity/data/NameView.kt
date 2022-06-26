package com.coworkerteam.coworker.unity.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.coworkerteam.coworker.R

class NameView  : ConstraintLayout {

    lateinit var view: View
    lateinit var nameView: TextView


    lateinit var nickname : String
    var x_coordination : Float = 0.0f
    var y_coordination : Float = 0.0f

    //생성자
    constructor(context: Context) : super(context) {
        init()
    }

    fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.item_whiteboard_nickname, this, true)
        nameView=  view.findViewById<TextView>(R.id.item_whiteboard_txt_nickname)
    }

    fun setXY(x:Float,y:Float){
        x_coordination = x
        y_coordination = y
        setX(x)
        setY(y)
    }

    fun setName(name : String){
        nickname = name
        nameView.setText(nickname)
    }

}