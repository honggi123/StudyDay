package com.coworkerteam.coworker.ui.camstudy.info
import android.util.Log

import android.app.Activity
import android.content.Context
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.ParticipantsResponse

class ParticipantsAdapter(private val context: Context,private val mServiceCallback: Messenger?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = "ParticipantsAdapter"

    val viewLeader = 0
    val viewNotLeader = 1

    var datas = mutableListOf<ParticipantsResponse.Participant>()
    var isLeader = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            viewLeader -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_participants_leader,
                    parent,
                    false
                )
                ViewHolderLeader(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_participants_member,
                    parent,
                    false
                )
                ViewHolderNotLeader(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun getItemViewType(position: Int): Int {

        if (datas[position].isLeader) {
            return viewLeader
        } else {
            return viewNotLeader
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (datas[position].isLeader) {
            (holder as ViewHolderLeader).bind(datas[position])
        } else {
            (holder as ViewHolderNotLeader).bind(datas[position])
        }
    }

    inner class ViewHolderLeader(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_leader_transfer_profile)
        private val nickname: TextView = itemView.findViewById(R.id.item_leader_transfer_nickname)

        fun bind(item: ParticipantsResponse.Participant) {
            Glide.with(context).load(item.img).into(img)

        }
    }

    inner class ViewHolderNotLeader(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_leader_transfer_profile)
        private val nickname: TextView = itemView.findViewById(R.id.item_leader_transfer_nickname)
        private val more: ImageView = itemView.findViewById(R.id.item_member_participants_more)

        fun bind(item: ParticipantsResponse.Participant) {
            Glide.with(context).load(item.img).into(img)

            nickname.text = item.nickname
            val par = item

            if(isLeader){
                more.setOnClickListener(View.OnClickListener {
                    var popup = PopupMenu(context, it)
                    var con = context as Activity
                    con.menuInflater?.inflate(R.menu.camstudy_participants_leader_menu, popup.menu)
                    popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(item: MenuItem?): Boolean {
                            when (item?.itemId) {
                                R.id.menu_forcedexit -> {
                                    Log.d(TAG ,"menu_forcedexit")
                                    val handlerMessage = Message.obtain(null,
                                        CamStudyService.MSG_LEADER_FORCED_EXIT
                                    )
                                    handlerMessage.obj = par.nickname
                                    sendHandlerMessage(handlerMessage)
                                }

                                R.id.menu_forced_audio ->{
                                    Log.d(TAG ,"menu_forced_audio")
                                    val handlerMessage = Message.obtain(null,
                                        CamStudyService.MSG_LEADER_FORCED_AUDIO_OFF
                                    )
                                    handlerMessage.obj = par.nickname
                                    sendHandlerMessage(handlerMessage)
                                }

                                R.id.menu_forced_video ->{
                                    Log.d(TAG ,"menu_forced_video")
                                    val handlerMessage = Message.obtain(null,
                                        CamStudyService.MSG_LEADER_FORCED_VIDEO_OFF
                                    )
                                    handlerMessage.obj = par.nickname
                                    sendHandlerMessage(handlerMessage)
                                }
                            }

                            return false
                        }
                    })
                    popup.show()
                })
            }else{
                more.visibility = View.GONE
            }
        }
    }

    private fun sendHandlerMessage(msg: Message) {
        if (mServiceCallback != null) {
            try {
                mServiceCallback!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            Log.d(TAG, "Send message to Service")
        }
    }

}