package com.coworkerteam.coworker.ui.yourday

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R


class SuccessPostAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private lateinit var items: ArrayList<String?>
    private lateinit var items_bgdrawble: ArrayList<Int>
    private lateinit var context : Context

    fun setdata(items: ArrayList<String?>,items_bgdrawble: ArrayList<Int>) {
        this.items = items
        this.items_bgdrawble = items_bgdrawble
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            context = parent.context
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_studysuccesspost, parent, false)
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
        var item_bg = 0
        if(position>=5){
            item_bg = position%5
        }else{
            item_bg = items_bgdrawble!![position]
        }

      //  holder.setItem(item,item_bg)

        holder.textView.text = item

        when (item_bg) {
            0 -> Glide.with(context).load(R.drawable.card_back1).into(holder.background)
            1 -> Glide.with(context).load(R.drawable.card_back2).into(holder.background)
            2 -> Glide.with(context).load(R.drawable.card_back3).into(holder.background)
            3 -> Glide.with(context).load(R.drawable.card_back4).into(holder.background)
            4 -> Glide.with(context).load(R.drawable.card_back5).into(holder.background)
            else -> Glide.with(context).load(R.drawable.card_back1).into(holder.background)
        }


    }

    private class ItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textView: TextView
         var background: ImageView

        init {
            textView = itemView.findViewById(R.id.studysuccess_myfeeling)
            background = itemView.findViewById(R.id.studysuccess_background)
        }
    }

    private class LoadingViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar

        init {
            progressBar = itemView.findViewById(R.id.progressBar)
        }
    }



}