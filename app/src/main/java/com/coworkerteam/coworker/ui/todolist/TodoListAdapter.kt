package com.coworkerteam.coworker.ui.todolist

import android.widget.Toast

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.TodolistResponse
import com.google.android.material.textfield.TextInputLayout

class TodoListAdapter(private val context: Context, private val viewModel: TodoListViewModel) :
    RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    val TAG = "TodolistAdapter"

    var datas = mutableListOf<TodolistResponse.Result.TheDayTodo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_todolist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val checkbox: CheckBox = itemView.findViewById(R.id.item_todolist_checkbox)
        private val more_menu: ImageView = itemView.findViewById(R.id.item_todolist_more_menu)

        fun bind(item: TodolistResponse.Result.TheDayTodo) {
            checkbox.text = item.todo
            checkbox.isChecked = item.isComplete
            val index = datas.indexOf(item)

            if (item.isComplete) {
                checkbox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                checkbox.setTextColor(Color.GRAY)
            }else{
                checkbox.paintFlags = 0
                checkbox.setTextColor(Color.BLACK)
            }

            var items = item
            Log.d("디버그태그", items.toString())

            checkbox.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        viewModel.setCheckTodoListData(item.idx, item.createDate)
                        checkbox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                        checkbox.setTextColor(Color.GRAY)

                        var theDayTodo = TodolistResponse.Result.TheDayTodo(items.createDate, index, isChecked, item.todo)

                        datas.removeAt(index)
                        datas.add(index, theDayTodo)

                    } else {
                        viewModel.setCheckTodoListData(item.idx, item.createDate)
                        checkbox.paintFlags = 0
                        checkbox.setTextColor(Color.BLACK)

                        var theDayTodo = TodolistResponse.Result.TheDayTodo(items.createDate, index, isChecked, item.todo)

                        datas.removeAt(index)
                        datas.add(index, theDayTodo)

                    }
                }
            )

            more_menu.setOnClickListener(View.OnClickListener {
                var popup = PopupMenu(context, it)
                var con = context as Activity
                con.menuInflater?.inflate(R.menu.todolist_popup_menu, popup.menu)
                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        when (item?.itemId) {
                            R.id.menu_modify -> {
                                val mDialogView =
                                    LayoutInflater.from(context)
                                        .inflate(R.layout.dialog_todo_list_edit, null)
                                val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                                val builder = mBuilder.show()

                                builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                                val txt_day =
                                    mDialogView.findViewById<TextInputLayout>(R.id.dialog_todolist_edt__txt_day)
                                val edt_todo =
                                    mDialogView.findViewById<TextInputLayout>(R.id.dialog_todolist_edt_edit)
                                val btn_cancle =
                                    mDialogView.findViewById<Button>(R.id.dialog_todolist_edt_btn_cancle)
                                val btn_remove =
                                    mDialogView.findViewById<Button>(R.id.dialog_todolist_edt_btn_remove)

                                txt_day.editText?.setText(items.createDate)
                                edt_todo.editText?.setText(items.todo)

                                btn_cancle.setOnClickListener(View.OnClickListener {
                                    builder.dismiss()
                                })

                                btn_remove.setOnClickListener(View.OnClickListener {
                                    editTodolist(
                                        items.createDate,
                                        edt_todo.editText?.text.toString(),
                                        items.idx,
                                        index,
                                        checkbox.isChecked
                                    )
                                    viewModel.setEditTodoListData(
                                        items.createDate,
                                        edt_todo.editText?.text.toString(),
                                        items.idx
                                    )
                                    builder.dismiss()
                                })
                            }

                            R.id.menu_delete -> {
                                removeTodolist(index)
                                viewModel.deleteTodoListData(items.idx, items.createDate)
                            }
                        }

                        return false
                    }
                })
                popup.show()
            })


        }
    }

    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // (4) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener: OnItemClickListener

    fun removeTodolist(position: Int) {
        datas.removeAt(position)
        notifyDataSetChanged()

    }

    fun editTodolist(selectDay: String, todo: String, idx: Int, position: Int, ischeck: Boolean) {
        var theDayTodo = TodolistResponse.Result.TheDayTodo(selectDay, idx, ischeck, todo)

        datas.removeAt(position)
        datas.add(position, theDayTodo)

        notifyDataSetChanged()
    }

}