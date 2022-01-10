package com.coworkerteam.coworker.ui.setting.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R

class MyProfileAdapter(private val context: Context) :
    RecyclerView.Adapter<MyProfileAdapter.ViewHolder>() {

    var datas = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_profile_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val category: TextView = itemView.findViewById(R.id.my_profile_edit_txt_test)

        fun bind(item: String) {
            category.text = item
            category.setSelected(true)
        }
    }
}