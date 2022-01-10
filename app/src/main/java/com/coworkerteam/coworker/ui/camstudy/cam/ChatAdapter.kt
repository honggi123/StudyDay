package com.coworkerteam.coworker.ui.camstudy.cam

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.other.Chat

class ChatAdapter(private val context: Context) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var datas = mutableListOf<Chat>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val name: TextView = itemView.findViewById(R.id.item_chat_nickname)
        private val time: TextView = itemView.findViewById(R.id.item_chat_time)
        private val context: TextView = itemView.findViewById(R.id.item_chat_context)

        fun bind(item: Chat) {

            name.text = item.sender
            time.text = item.time
            context.text = item.msg

        }
    }
}