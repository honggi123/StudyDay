package com.coworkerteam.coworker.ui.yourday

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R

class MoodPostAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private lateinit var items: ArrayList<String?>
    private var dialog_showing : Boolean = false

    fun setdata(items: ArrayList<String?>) {
        this.items = items

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_moodpost, parent, false)
            ItemViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is LoadingViewHolder) {
            showLoadingView(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items!![position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return if (items == null) 0 else items!!.size
    }

    private fun showLoadingView(holder: LoadingViewHolder, position: Int) {}

    private fun populateItemRows(holder: ItemViewHolder, position: Int) {
        val item = items!![position]

        holder.txt_empathy.setOnClickListener(View.OnClickListener {
            if (dialog_showing){
                holder.dialog_empathy.visibility = View.GONE
                dialog_showing = false
            }else{
                holder.dialog_empathy.visibility = View.VISIBLE
                dialog_showing = true
            }

        })
    }
    }

    private class ItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var txt_empathy: TextView
        var dialog_empathy: ConstraintLayout
        init {
            txt_empathy = itemView.findViewById(R.id.txt_empathy)
            dialog_empathy = itemView.findViewById(R.id.dialog_empathy)
        }
    }

    private class LoadingViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar

        init {
            progressBar = itemView.findViewById(R.id.progressBar)
        }
    }

