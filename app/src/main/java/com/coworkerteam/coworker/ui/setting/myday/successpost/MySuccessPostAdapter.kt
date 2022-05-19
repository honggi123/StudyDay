package com.coworkerteam.coworker.ui.setting.myday.successpost

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.SuccessPostResponse
import com.coworkerteam.coworker.ui.setting.myday.MydayActivity
import com.coworkerteam.coworker.ui.setting.myday.MydayViewModel
import com.coworkerteam.coworker.ui.yourday.YourdayViewModel
import com.coworkerteam.coworker.utils.DateFormatUtils


class MySuccessPostAdapter(private val viewModel: MydayViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TAG = "MySuccessPostAdapter"
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private lateinit var items: ArrayList<SuccessPostResponse.Result.SuccessPost?>
    private lateinit var context : MydayActivity
    var removeitem : SuccessPostResponse.Result.SuccessPost? = null

    fun setdata(items: ArrayList<SuccessPostResponse.Result.SuccessPost?>) {
        this.items = items
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            context = parent.context as MydayActivity
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
            item_bg = position
        }
        when (item_bg) {
            0 -> Glide.with(context).load(R.drawable.card_back1).into(holder.background)
            1 -> Glide.with(context).load(R.drawable.card_back2).into(holder.background)
            2 -> Glide.with(context).load(R.drawable.card_back3).into(holder.background)
            3 -> Glide.with(context).load(R.drawable.card_back4).into(holder.background)
            4 -> Glide.with(context).load(R.drawable.card_back5).into(holder.background)
            else -> Glide.with(context).load(R.drawable.card_back1).into(holder.background)
        }
        holder.nickname.setText(item?.nickname)

        var create_date = DateFormatUtils.daysToStringformat_successPost(item!!.create_date)
        holder.date.setText(create_date)
        holder.feeling.setText(item?.contents)

        var successtime = DateFormatUtils.secondsToHourMin(item!!.success_time)
        holder.time.setText("목표시간 "+successtime+"을 달성하셨습니다.")

        if (item?.nickname?.equals(viewModel.getUserName())!!){
            holder.view_remove.visibility = View.VISIBLE
        }else{
            holder.view_remove.visibility = View.INVISIBLE
        }

        holder.view_remove.setOnClickListener(View.OnClickListener {
            if (item != null) {
                context.firebaseLog.addLog(TAG,"delete")
                viewModel.deleteSuccessPostdData(item.idx)
                removeitem = item
            }
        })
    }

    private class ItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
         val feeling: TextView
         var background: ImageView
         val nickname: TextView
         val date: TextView
         val time: TextView
         val view_remove : ImageView
        init {
            feeling = itemView.findViewById(R.id.item_success_post_myfeeling)
            background = itemView.findViewById(R.id.studysuccess_background)
            nickname = itemView.findViewById(R.id.item_success_post_nickname)
            date = itemView.findViewById(R.id.item_success_post_date)
            time = itemView.findViewById(R.id.item_success_post_goaltime)
            view_remove = itemView.findViewById(R.id.item_success_post_removeicon)
        }
    }

    private class LoadingViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar
        init {
            progressBar = itemView.findViewById(R.id.progressBar)
        }
    }

    fun adddata(datas: ArrayList<SuccessPostResponse.Result.SuccessPost>?) {
        if (datas != null) {
            items.addAll(datas)
        }
        notifyDataSetChanged()
    }

}