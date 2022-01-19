package com.coworkerteam.coworker.ui.study.leader.transfer

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.prefs.AppPreferencesHelper
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.StudyMemberResponse
import com.coworkerteam.coworker.data.remote.StudydayService
import com.google.android.gms.common.api.ApiException
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

class LeaderTransferAdapter(
    private val context: Context,
    private val viewModel: LeaderTransferViewModel
) :
    RecyclerView.Adapter<LeaderTransferAdapter.ViewHolder>() {

    val TAG = "LeaderTransferAdapter"

    var datas = mutableListOf<StudyMemberResponse.Result>()
    var emptyView: View? = null
    var study_idx = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_study_leader_transfer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(datas.size <= 0){
            emptyView!!.visibility = View.VISIBLE
        }else{
            emptyView!!.visibility = View.GONE
        }
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_leader_transfer_profile)
        private val leader: Button = itemView.findViewById(R.id.item_leader_transfer_btn)
        private val nickname: TextView = itemView.findViewById(R.id.item_leader_transfer_nickname)

        fun bind(item: StudyMemberResponse.Result) {
            Glide.with(context).load(item.img).into(img)

            nickname.text = item.nickname

            leader.setOnClickListener(View.OnClickListener {
                val mDialogView =
                    LayoutInflater.from(context).inflate(R.layout.dialog_leader_transfer, null)
                val mBuilder = AlertDialog.Builder(context).setView(mDialogView)
                val builder = mBuilder.show()

                builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val btn_cancle =
                    mDialogView.findViewById<Button>(R.id.dialog_leader_transfer_btn_cancle)
                val btn_logout =
                    mDialogView.findViewById<Button>(R.id.dialog_leader_transfer_btn_leader)

                btn_cancle.setOnClickListener(View.OnClickListener {
                    builder.dismiss()
                })

                btn_logout.setOnClickListener(View.OnClickListener {
                    viewModel.setLeaderTransferData(item.idx, study_idx)
                    datas.remove(item)

                    notifyDataSetChanged()
                })
            })
        }
    }
}