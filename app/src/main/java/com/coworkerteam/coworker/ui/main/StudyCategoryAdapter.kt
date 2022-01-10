package com.coworkerteam.coworker.ui.main

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.utils.ScreenSizeUtils

class StudyCategoryAdapter(private val context: Context) :
    RecyclerView.Adapter<StudyCategoryAdapter.ViewHolder>() {

    var datas = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_study_categoty, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])

        // 간격 설정
//        val layoutParams = holder.itemView.layoutParams
//        layoutParams.width = (ScreenSizeUtils().getScreenWidthSize(context as Activity)/6.75).toInt()
//        holder.itemView.requestLayout()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val category:TextView = itemView.findViewById(R.id.item_study_category)

        fun bind(item: String) {
            category.text = item
        }
    }
}