package com.coworkerteam.coworker.ui.camstudy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R

class CamStudyCategotyAdapter(private val context: Context) :
    RecyclerView.Adapter<CamStudyCategotyAdapter.ViewHolder>() {

    var datas = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_camstudy_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val category: TextView = itemView.findViewById(R.id.item_camstudy_category)

        fun bind(item: String) {
            category.text = item
        }
    }
}