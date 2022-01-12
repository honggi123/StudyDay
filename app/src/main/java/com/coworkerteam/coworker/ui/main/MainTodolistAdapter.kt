package com.coworkerteam.coworker.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.MainResponse

class MainTodolistAdapter(private val context: Context, private val viewModel: MainViewModel) :
    RecyclerView.Adapter<MainTodolistAdapter.ViewHolder>() {

    val TAG = "MainTodolistAdapter"

    var datas = mutableListOf<MainResponse.Result.Todo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_home_todolist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val checkbox: CheckBox = itemView.findViewById(R.id.item_home_check_todolist)

        fun bind(item: MainResponse.Result.Todo) {
            checkbox.text = item.todo
            checkbox.isChecked = item.isComplete

            var items = item

            checkbox.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        viewModel.setCheckTodoListData(item.idx, item.todoDate)
                    } else {
                        viewModel.setCheckTodoListData(item.idx, item.todoDate)
                    }
                }
            )
        }
    }
}