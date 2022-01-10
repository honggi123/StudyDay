package com.coworkerteam.coworker.ui.camstudy.cam

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.CamStudyService
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.other.Participant
import de.hdodenhof.circleimageview.CircleImageView
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoRenderer

class CamStudyAdapter(private val context: Context) :
    RecyclerView.Adapter<CamStudyAdapter.ViewHolder>() {

    var datas = mutableListOf<String>()
    lateinit var hashmap: HashMap<String, Participant>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_camstudy, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val img: CircleImageView = itemView.findViewById(R.id.item_camstudy_profile)
        private val name: TextView = itemView.findViewById(R.id.item_camstudy_txt_name)
        private val time: TextView = itemView.findViewById(R.id.item_camstudy_txt_time)
        private val surface: SurfaceViewRenderer = itemView.findViewById(R.id.cam_surface_view)

        private val startTimer :ImageView = itemView.findViewById(R.id.imageView8)
        private val mic:ImageView = itemView.findViewById(R.id.imageView9)

        fun bind(item: String) {
            var user = hashmap.get(item)

            surface.init(CamStudyService.rootEglBase?.getEglBaseContext(), null)
            surface.setEnableHardwareScaler(true)
            surface.setMirror(true)

            time.text = user?.time

            user?.profileView = img
            user?.txt_time = time
            user?.playStateImage = startTimer
            user?.image_mic = mic
            user?.setRender(surface)

            if(user!!.img != null){
                Glide.with(context).load(user!!.img).into(img)
            }

            if(user!!.isStartTimer == true){
                startTimer.isSelected = false
            }else if(user!!.isStartTimer == false || user!!.isStartTimer == null){
                startTimer.isSelected = true
            }

            if (user?.remoteVideoTrack != null) {
                user?.startRender()
                img.visibility = View.GONE
            }

            name.text = item
            time.text = user?.time

//            Glide.with(context).load(item.img).into(img)
//
//            if (!item.isLeader) {
//                leader.visibility = View.GONE
//            }
//
//            studyName.text = item.name

        }
    }
}