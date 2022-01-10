package com.coworkerteam.coworker.ui.todolist

import android.widget.Toast

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.CheckTodolistRequest
import com.coworkerteam.coworker.data.model.api.DeleteTodolistRequest
import com.coworkerteam.coworker.data.model.api.EditTodolistResponse
import com.coworkerteam.coworker.data.model.dto.TheDayTodo
import com.coworkerteam.coworker.data.remote.StudydayService
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class TodoListAdapter(private val context: Context, private val viewModel: TodoListViewModel) :
    RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    val TAG = "TodolistAdapter"

    var datas = mutableListOf<TheDayTodo>()

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

        fun bind(item: TheDayTodo) {
            checkbox.text = item.todo
            checkbox.isChecked = item.isComplete

            var items = item
            Log.d("디버그태그", items.toString())

            checkbox.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        viewModel.setCheckTodoListData(item.idx, item.createDate)
                    } else {
                        viewModel.setCheckTodoListData(item.idx, item.createDate)
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
                                    mDialogView.findViewById<TextView>(R.id.dialog_todolist_edt__txt_day)
                                val edt_todo =
                                    mDialogView.findViewById<EditText>(R.id.dialog_todolist_edt_edit)
                                val btn_cancle =
                                    mDialogView.findViewById<Button>(R.id.dialog_todolist_edt_btn_cancle)
                                val btn_remove =
                                    mDialogView.findViewById<Button>(R.id.dialog_todolist_edt_btn_remove)

                                txt_day.text = items.createDate
                                edt_todo.setText(items.todo)

                                btn_cancle.setOnClickListener(View.OnClickListener {
                                    builder.dismiss()
                                })

                                btn_remove.setOnClickListener(View.OnClickListener {
                                    editTodolist(
                                        items.createDate,
                                        edt_todo.text.toString(),
                                        items.idx,
                                        adapterPosition,
                                        items.isComplete
                                    )
                                    viewModel.setEditTodoListData(
                                        items.createDate,
                                        edt_todo.text.toString(),
                                        items.idx
                                    )
                                    builder.dismiss()
                                })
                            }

                            R.id.menu_delete -> {
                                removeTodolist(adapterPosition)
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
        var theDayTodo = TheDayTodo(selectDay, idx, ischeck, todo)

        datas.removeAt(position)
        datas.add(position, theDayTodo)

        notifyDataSetChanged()
    }

}