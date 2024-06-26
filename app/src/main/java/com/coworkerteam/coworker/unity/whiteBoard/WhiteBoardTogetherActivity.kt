package com.coworkerteam.coworker.unity.whiteBoard

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.local.service.CamStudyService
import com.coworkerteam.coworker.data.local.service.WhiteBoardService
import com.coworkerteam.coworker.databinding.ActivityWhiteboardtogetherBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.dialog.SketchChoiceDialog
import com.coworkerteam.coworker.unity.data.Path_info
import com.coworkerteam.coworker.unity.data.Xy
import com.coworkerteam.coworker.unity.data.NameView
import com.coworkerteam.coworker.unity.data.Whiteboard_Participant
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class WhiteBoardTogetherActivity : BaseActivity<ActivityWhiteboardtogetherBinding, WhiteBoardTogetherViewModel>() {

    val TAG = "WhiteBoardTogetherActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_whiteboardtogether
    override val viewModel: WhiteBoardTogetherViewModel by viewModel()
    var canvas: ConstraintLayout? = null                // 실제 그리기 캔버스
    var canvas_checkwidth: LinearLayout? = null      // 그리기 메뉴를 켰을때 예시로 그려지는 캔버스

    var menu : Menu? = null
    lateinit var drawingPanel : DrawingPaneltogether
    lateinit var drawingPanel_checksize : DrawingPanel_CheckWidth       // 그리기 메뉴를 켰을때 예시로 그려지는 캔버스의 그리기 굵기 저장을 위한 변수

    var allpaths_info = ArrayList<Path_info>()      // 모든 그리기 객체를 담은 배열
    var participants = ArrayList<Whiteboard_Participant>()      // 화이트 보드 참여자 정보를 담은 배열

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    var name : String = ""          // 자신의 닉네임
    var roomLink : String? = ""     // 참여중인 방 링크
    lateinit var profileURL : String    // 현재 자신의 프로필 링크

    var countUndoWhenDrawing : Int = 0      // 그리고 있는 도중 상대방이 실행취소 한 횟수
                                            // 현재 그리고 있는 그리기 객체 값을 배열에서 꺼내서 사용한다.
                                            // 하지만 그리는 도중 실행취소를 한 경우 배열의 사이즈가 줄어들어 같은 인덱스 값으로 그리기 객체를 찾을 수 없기 때문에

    val FILE_NAME = "studyday"          // 이미지를 저장할때 사용하는 파일 이름

    lateinit var  layout : LinearLayout

    //서비스와 통신하는 Messenger 객체
    private var mServiceCallback: Messenger? = null
    private var mClientCallback = Messenger(CallbackHandler(Looper.getMainLooper()))

    override fun initStartView() {
        Log.d(TAG,"initStartView")
        roomLink = intent.getStringExtra("roomLink")
       // roomLink = "https://www.studyday.co.kr/link?idx=211?pwd=null"
        canvas_checkwidth = findViewById<View>(R.id.whiteboard_canvas_check_width) as LinearLayout
        drawingPanel_checksize = DrawingPanel_CheckWidth(this)
        canvas_checkwidth!!.addView(drawingPanel_checksize)

        canvas = findViewById<View>(R.id.whiteboard_canvas) as ConstraintLayout
        drawingPanel = DrawingPaneltogether(this)
        canvas!!.addView(drawingPanel)

        viewDataBinding.activitiy = this
        viewDataBinding.drawingpanel = drawingPanel
        viewDataBinding.zoomdirection = drawingPanel.zoomdirection

        layout = findViewById<LinearLayout>(R.id.dialog_participant_list)

         name = viewModel.getUserName().toString()
      //   name = "honghong5"

        profileURL = viewModel.getProfileIMG().toString()
      //  profileURL = "https://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_110x110.jpg"
        //툴바 세팅
        var main_toolbar = viewDataBinding.toolbarWhiteboard as androidx.appcompat.widget.Toolbar


        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(false) // 드로어를 꺼낼 홈 버튼 활성화

        var btn_undo = findViewById<ImageView>(R.id.whiteboard_toolbar_undo)
        var btn_redo = findViewById<ImageView>(R.id.whiteboard_toolbar_redo)
        var btn_zoom = findViewById<ImageView>(R.id.whiteboard_toolbar_zoom)
        var btn_show_participants = findViewById<LinearLayout>(R.id.whiteboard_btn_show_participants)


        btn_redo.setOnClickListener(View.OnClickListener {
            drawingPanel.redo()
        })

        btn_undo.setOnClickListener(View.OnClickListener {
            drawingPanel.undo()
        })

        btn_zoom.setOnClickListener(View.OnClickListener {
            if(!drawingPanel.zoomStatus){
                drawingPanel.setZoomMode()
                btn_zoom.setImageResource(R.drawable.ic_baseline_zoom_out_24)

            }else{
                drawingPanel.setReleaseZoomMode()
                btn_zoom.setImageResource(R.drawable.ic_baseline_zoom_in_24)
            }
            viewDataBinding.zoomdirection = drawingPanel.zoomdirection
        })

        btn_show_participants.setOnClickListener(View.OnClickListener {
            showParticipantList()
        })

        viewDataBinding.seekBarStrokewidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingPanel.setStrokeWidth(progress.toFloat())
                drawingPanel_checksize.setSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        viewDataBinding.seekBarEraseStrokewidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawingPanel.setEraseStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })


        var intent = Intent(this, WhiteBoardService::class.java)
        intent.putExtra("name",name)
        intent.putExtra("roomLink",roomLink)
        intent.putExtra("profileURL",profileURL)

        startForegroundService(intent)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isServiceRunningCheck(WhiteBoardService::class.java.name)){
            unbindService(mConnection)
            stopService(Intent(this, WhiteBoardService::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        showExitDialog()
    }

    @SuppressWarnings("deprecation")
    fun isServiceRunningCheck(servicename:String) : Boolean{
        var manager : ActivityManager =  this.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        for (service : ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d(TAG,"service.service.getClassName() ; " + service.service.getClassName())
            if (servicename.equals(service.service.getClassName())) {
                return true
            }
        }
        return false
    }


    // 나가기 다이얼로그
    fun showExitDialog(){
        val mDialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_whiteboard_exit, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        val builder = mBuilder.show()

        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btn_cancle =
            mDialogView.findViewById<Button>(R.id.dialog_whiteboard_exit_btn_cancle)
        val btn_ok = mDialogView.findViewById<Button>(R.id.dialog_whiteboard_exit_btn_ok)

        btn_cancle.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })

        btn_ok.setOnClickListener(View.OnClickListener {
            var msg: Message? = null
            msg = Message.obtain(null, WhiteBoardService.MSG_LEAVE_ROOM)

            sendHandlerMessage(msg)
            builder.dismiss()
        })
    }




    // 터치 이벤트 처리
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.whiteboard_menu, menu)
        this.menu = menu!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_save->{
                Log.d(TAG,"SAVE")
                val permissionListener: PermissionListener = object: PermissionListener {
                    override fun onPermissionGranted() {
                        //권한 허가시 실행할 내용
                        drawingPanel.save(this@WhiteBoardTogetherActivity)
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        // 권한 거부시 실행  할 내용
                        Toast.makeText(this@WhiteBoardTogetherActivity,"권한을 허용하지 않으면 그림을 저장 할 수 없습니다..",Toast.LENGTH_SHORT).show()

                    }
                }
                TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage("해당 권한을 [설정] > [권한] 에서 허용해주세요.")
                    .setPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    )
                    .check()

            }
            R.id.menu_out->{
                showExitDialog()
            }

        }
        return super.onOptionsItemSelected(item)
    }



    override fun initDataBinding() {

    }

    override fun initAfterBinding() {
    }

    private fun initView() {
    }

    fun showOtherName(name : String, x: Float,y:Float){
        coroutineScope.launch {
            var nameview = NameView(context = applicationContext)
            nameview.setName(name)
            nameview.setXY(x,y)
            canvas!!.addView(nameview)
            delay(1000)
            canvas!!.removeView(nameview)
        }
    }

    inner class CallbackHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, "Activity에 메세지 도착 : $msg")
            when (msg.what) {
                CamStudyService.MSG_SERVICE_CONNECT -> {
                    //연결 성공했을 경우

                    var participant = Whiteboard_Participant()
                    participant.nickname = name
                    participant.profileImg = profileURL
                    participants.add(participant)
                }
                WhiteBoardService.MSG_RECEIVE_ROOM_DATA ->{
                    Log.d(TAG,"MSG_RECEIVE_ROOM_DATA")
                    var data = msg.data
                    var gson = Gson()
                    var json = JSONObject(data.get("data").toString())
                    Log.d(TAG,json.toString())

                    if (!json.isNull("canvasList")){
                        var jsonArray =  JSONArray(json.getString("canvasList"))
                        for (i in 0..jsonArray.length() -1){
                            Log.d(TAG,jsonArray.length().toString())
                            var path = gson.fromJson(jsonArray.get(i).toString(),Path_info::class.java)
                            allpaths_info.add(path)
                        }
                    }

                    if (!json.isNull("sketchNum")){
                        WhiteBoardService.sketchNum = json.getInt("sketchNum")
                        var num = json.getInt("sketchNum")
                        var resid = resources.getIdentifier("SketchURL$num","string",packageName)
                        SetSketchURL(resources.getString(resid))
                    }else{
                        WhiteBoardService.sketchNum = 0
                    }

                    var viewParticipantNum = findViewById<TextView>(R.id.whiteboard_txt_particiantenum)
                    viewParticipantNum.setText(json.getInt("participantNum").toString()+"명")
                    if(json.getInt("participantNum")>1){
                        var participantView = findViewById<ImageView>(R.id.whiteboard_icon_particiantenum)
                        participantView.setBackgroundResource(R.drawable.ic_baseline_people_outline_24)
                    }

                    for(p in allpaths_info){
                        p.setpaint(Paint())
                        p.setpath(Path())
                        p.paint .isAntiAlias = true
                        p.paint .color = Color.BLACK
                        p.paint .style = Paint.Style.STROKE
                        p.paint .strokeJoin = Paint.Join.ROUND
                        p.paint .strokeCap = Paint.Cap.ROUND
                        p.paint.strokeWidth = p.penwidth
                        p.paint.color =  Color.parseColor(p.pencolor)

                        if(p.shapemode == true){
                            p.setshapetype(p.shapetype)
                        }else if(p.penmode == true){
                            p.setpentype(p.pentype)
                        }

                        setPath(p.path,p.listxy)
                    }

                    drawingPanel.invalidate()

                    if (!json.isNull("participant")){
                        var jsonArray =  JSONArray(json.getString("participant"))
                        for (i in 0..jsonArray.length() -1){
                            var participant = gson.fromJson(jsonArray.get(i).toString(),Whiteboard_Participant::class.java)

                            if(!participant.nickname.equals(name)){
                                participants.add(participant)
                            }
                        }
                    }

                }WhiteBoardService.MSG_SEND_REMOVE_ACTION ->{
                drawingPanel.clearAll()
            }WhiteBoardService.MSG_RECEIVE_DRAWING->{
                Log.d(TAG,"MSG_RECEIVE_DRAWING")

                var data = msg.data
                var gson = Gson()
                var path_info = gson.fromJson(data.get("data").toString(),Path_info::class.java)

                path_info.setpaint(Paint())
                path_info.setpath(Path())
                path_info.paint .isAntiAlias = true
                path_info.paint .color = Color.BLACK
                path_info.paint .style = Paint.Style.STROKE
                path_info.paint .strokeJoin = Paint.Join.ROUND
                path_info.paint .strokeCap = Paint.Cap.ROUND
                path_info.paint.strokeWidth = path_info.penwidth
                var paths_size = path_info.listxy.size
                path_info.name?.let { showOtherName(it,path_info.listxy.get(paths_size-1).x,path_info.listxy.get(paths_size-1).y) }

                path_info.paint.color =  Color.parseColor(path_info.pencolor)
                if(path_info.shapemode == true){
                    path_info.setshapetype(path_info.shapetype)
                }else if(path_info.penmode == true){
                    path_info.setpentype(path_info.pentype)
                }

                setPath(path_info.path,path_info.listxy)
                allpaths_info.add(path_info)
                drawingPanel.invalidate()
            }WhiteBoardService.MSG_RECEIVE_SKETCH->{
                Log.d(TAG,"MSG_RECEIVE_SKETCH")
                var data = msg.data
                var json = JSONObject(data.get("data").toString())
                Log.d(TAG,json.toString())
                if (!json.isNull("sketchNum")){
                    var num = json.getInt("sketchNum")
                    WhiteBoardService.sketchNum = num
                    if(num == 0){
                        drawingPanel.sketch = null
                    }else{
                        var resid = resources.getIdentifier("SketchURL$num","string",packageName)
                        SetSketchURL(resources.getString(resid))
                    }
                    drawingPanel.invalidate()
                }else{
                    WhiteBoardService.sketchNum = 0
                }

            }WhiteBoardService.MSG_RECEIVE_ACTION -> {
                Log.d(TAG,"MSG_RECEIVE_SKETCH")
                var data = msg.data
                var json = JSONObject(data.get("data").toString())
                if(json.getString("actionName").equals("undo")){
                    if(allpaths_info.size>0){
                        for (i in allpaths_info.size-1 downTo 0){
                            if(allpaths_info.get(i).name.equals(json.getString("nickname"))){
                                allpaths_info.removeAt(i)
                                countUndoWhenDrawing++
                                break
                            }
                        }
                    }
                }else if(json.getString("actionName").equals("remove")){
                    drawingPanel.clearAll()
                }
                drawingPanel.invalidate()

            }WhiteBoardService.MSG_RECEIVE_INTO ->{
                Log.d(TAG,"MSG_RECEIVE_INTO")
                var data = msg.data
                var json = JSONObject(data.get("data").toString())

                var viewParticipantNum = findViewById<TextView>(R.id.whiteboard_txt_particiantenum)
                viewParticipantNum.setText(json.getInt("participantNum").toString()+"명")
                if(json.getInt("participantNum")>1){
                    var participantView = findViewById<ImageView>(R.id.whiteboard_icon_particiantenum)
                    participantView.setBackgroundResource(R.drawable.ic_baseline_people_outline_24)
                }

                coroutineScope.launch {
                    var textview= findViewById<TextView>(R.id.toast_whiteboard_text)
                    textview.setText(json.getString("nickname")+"님이 입장하셨습니다.")
                    textview.visibility = View.VISIBLE
                    delay(3000)
                    textview.visibility = View.GONE
                }

                var participant = Whiteboard_Participant()
                participant.nickname = json.getString("nickname")
                participant.profileImg = json.getString("profileImg")
                participants.add(participant)

                if(layout.visibility == View.VISIBLE){
                    var layout_participant = LayoutInflater.from(this@WhiteBoardTogetherActivity).inflate(R.layout.item_whiteboard_participant, null)

                    var view_nickname = layout_participant.findViewById<TextView>(R.id.item_nickname)

                    if(participant.nickname.equals(name)){
                        view_nickname.setText(participant.nickname+" (나)")
                    }else{
                        view_nickname.setText(participant.nickname)
                    }
                    var profile =
                        layout_participant.findViewById<CircleImageView>(R.id.item_profile)
                    Glide.with(this@WhiteBoardTogetherActivity).load(participant.profileImg).into(profile)
                    layout.addView(layout_participant)
                }



            }WhiteBoardService.MSG_RECEIVE_LEAVE->{
                Log.d(TAG,"MSG_RECEIVE_LEAVE")
                var data = msg.data
                var json = JSONObject(data.get("data").toString())
                var viewParticipantNum = findViewById<TextView>(R.id.whiteboard_txt_particiantenum)
                viewParticipantNum.setText(json.getInt("participantNum").toString()+"명")
                if(json.getInt("participantNum")>1){
                    var participantView = findViewById<ImageView>(R.id.whiteboard_icon_particiantenum)
                    participantView.setBackgroundResource(R.drawable.ic_baseline_people_outline_24)
                }

                for(i in 0.. participants.size-1){
                    if(participants[i].nickname.equals(json.getString("nickname"))){
                        participants.remove(participants[i])
                        var  layout = findViewById<LinearLayout>(R.id.dialog_participant_list)

                        if(layout.size>0){
                            layout.removeViewAt(i)
                        }
                        break
                    }
                }

            }WhiteBoardService.MSG_LEAVE_ROOM->{
                unbindService(mConnection)
                stopService(Intent(this@WhiteBoardTogetherActivity, WhiteBoardService::class.java))
                finish()
            }
            }
        }

    }

    fun showParticipantList(){
        var  layout = findViewById<LinearLayout>(R.id.dialog_participant_list)

        if(layout.visibility == View.GONE) {
            layout.removeAllViews()

            for (p in participants) {
                var layout_participant = LayoutInflater.from(this).inflate(R.layout.item_whiteboard_participant, null)

                var view_nickname = layout_participant.findViewById<TextView>(R.id.item_nickname)

                if(name.equals(p.nickname)){
                    view_nickname.setText(p.nickname+" (나)")
                }else{
                    view_nickname.setText(p.nickname)
                }
                    var profile =
                        layout_participant.findViewById<CircleImageView>(R.id.item_profile)
                    Glide.with(this).load(p.profileImg).into(profile)
                    layout.addView(layout_participant)
            }
            layout.visibility = View.VISIBLE

        }else{
            layout.visibility = View.GONE
        }
    }

    private fun setPath(path: Path, listxy : java.util.ArrayList<Xy>, ){
        path.moveTo(listxy.get(0).x,listxy.get(0).y)
        for(i in 1..listxy.size-1){
            path.lineTo(listxy.get(i).x,listxy.get(i).y)
        }

    }

    fun showPenMenu(){
        var visible = viewDataBinding.dialogPenSelect.visibility
        drawingPanel.hideMenu()

        setVisible(visible,viewDataBinding.dialogPenSelect)
    }

    fun showEraseMenu(){
        var visible = viewDataBinding.dialogEraseSelect.visibility

        drawingPanel.hideMenu()

        setVisible(visible,viewDataBinding.dialogEraseSelect)
        drawingPanel.setEraseMode()
    }


    // 서비스 바인딩, 통신
    private var mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e(TAG,"ServiceConnection")
            mServiceCallback = Messenger(service)
            //서비스랑 연결
            val connectMsg = Message.obtain(null, WhiteBoardService.MSG_CLIENT_CONNECT)
            connectMsg.replyTo = mClientCallback

            try {
                mServiceCallback!!.send(connectMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceCallback = null
        }
    }


    private fun sendHandlerMessage(msg: Message){
        if (mServiceCallback != null) {
            try {
                mServiceCallback!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }



    fun SetSketchURL(url : String){
        if(!url.equals("")){
            var sketchBitmap : Bitmap? = null

            coroutineScope.launch {
                val originalDeferred = coroutineScope.async(Dispatchers.IO) {
                    getOriginalBitmap(url)
                }
                sketchBitmap = originalDeferred.await()
                sketchBitmap?.let { drawingPanel.setSketchImg(it) }
            }
        }else{
            drawingPanel.sketch = null
            drawingPanel.invalidate()
        }
    }



    private fun getOriginalBitmap(url : String) =
        URL(url).openStream().use{
            BitmapFactory.decodeStream(it)
        }

    fun showColorPicker(){
        var visible = viewDataBinding.dialogColorpicker.visibility

        drawingPanel.hideMenu()

        viewDataBinding.dialogColorpickerPallete.color = drawingPanel.prevColor

        setVisible(visible,viewDataBinding.dialogColorpicker)
    }

    fun setColor(){
        drawingPanel.setPaintColor(viewDataBinding.dialogColorpickerPallete.color)
        setVisible(View.VISIBLE,viewDataBinding.dialogColorpicker)
    }



    fun changeZoomDirection(direction: String){
        drawingPanel.setZoomDirection(direction)
        viewDataBinding.zoomdirection = drawingPanel.zoomdirection
    }

    fun showSketchMenu(){

        drawingPanel.hideMenu()

        SketchChoiceDialog.Builder(this,object : SketchChoiceDialog.DialogListener {
            override fun clickBtn(url: String?, sketchNum: Int) {

                if (url != null && sketchNum != WhiteBoardService.sketchNum) {
                    SetSketchURL(url)
                    WhiteBoardService.sketchNum = sketchNum
                    var msg: Message? = null
                    msg = Message.obtain(null, WhiteBoardService.MSG_SEND_SKETCH)
                    msg.arg1 = sketchNum
                    sendHandlerMessage(msg)
                }
            }},WhiteBoardService.sketchNum).show()

        //SketchChoiceDialog.Builder(this).show()
    }

    fun showShapeMenu(){
        var visible = viewDataBinding.dialogShapeSelect.visibility

        drawingPanel.hideMenu()

        setVisible(visible,viewDataBinding.dialogShapeSelect)
    }


    fun clear(){
        val mDialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_whiteboard_clear, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        val builder = mBuilder.show()

        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btn_ok =
            mDialogView.findViewById<Button>(R.id.dialog_btn_ok)
        val btn_cancle =
            mDialogView.findViewById<Button>(R.id.dialog_btn_cancle)

        btn_ok.setOnClickListener(View.OnClickListener {
            var msg: Message? = null
            msg = Message.obtain(null, WhiteBoardService.MSG_SEND_REMOVE_ACTION)
            sendHandlerMessage(msg)
            builder.dismiss()
        })

        btn_cancle.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })
    }

    fun setVisible(visible :Int,view: View){
        view.visibility = if(visible == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }



    inner class DrawingPaneltogether(context: Context?) : View(context),
        View.OnTouchListener {
        private val mCanvas: Canvas
        private var mPath: Path
        private var mPaint: Paint
        private val outercirclePaint: Paint
        var path = Path_info()
        var path_count = 0
        // private ArrayList<Path> undonePaths = new ArrayList<Path>();
        var prevColor : Int = Color.BLACK
        var prevWidth : Float = 10f

        var penMode : Boolean = false
        var pentype : Int = 1

        var sketch : Bitmap? = null

        private val undonePaths = ArrayList<Path_info>()

        var shapeMode : Boolean = false
        var shapetype : Int = 0

        var eraseMode : Boolean =false

        lateinit var stroke : Stroke

        var erasestrokewidth : Float = 10f
        // 확대관련 변수
        var zoomStatus = false
        var zoomDrawCount = 0
        var zoomdirection = 0

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)

        }

        // 최종적으로 배열에 있는 그리기 객체 값을 가져와서 그려주는 메소드
        override fun onDraw(canvas: Canvas) {
            if(zoomStatus){
                when(zoomdirection){
                    1->{
                        canvas.scale(2f, 2f,0f,0f)
                    }
                    2->{
                        canvas.scale(2f, 2f,this.width.toFloat(),0f)
                    }
                    3->{
                        canvas.scale(2f, 2f,0f,this.height.toFloat())
                    }
                    4->{
                        canvas.scale(2f, 2f,this.width.toFloat(),this.height.toFloat())
                    }
                }
            }
            for (p in allpaths_info) {
                // 도형 그리기 일 경우
                if(p.shapemode){
                    when(p.shapetype){
                        1->{
                            var rect =RectF(p.listxy.get(0).x,p.listxy.get(0).y,
                                p.shapeRightX, p.shapeRightY
                            )
                            canvas.drawArc(rect, 0F, 360F, false, p.paint);
                        }
                        2->{
                            drawTriangle(canvas, p.paint, p.listxy.get(0).x, p.listxy.get(0).y, p.shapeRightX,p.shapeRightY);
                        }
                        3->{
                            var rect =Rect(p.listxy.get(0).x.toInt(),p.listxy.get(0).y.toInt(),
                                p.shapeRightX.toInt(), p.shapeRightY.toInt()
                            )
                            canvas.drawRect(rect, p.paint)
                        }
                    }
                }else{
                    // 일반 그리기 또는 지우기 일 경우
                    canvas.drawPath(p.path, p.paint)
                }
            }
            if(sketch != null){
                drawSketchBitmap(canvas)
            }
        }

        // 삼각형을 그릴 수 있는 메소드
        fun drawTriangle(canvas : Canvas,  paint : Paint, x : Float,  y : Float, currentX :Float,currentY : Float){
            val halfWidth = width / 2
            val path = Path()
            path.moveTo(x + ((currentX - x) / 2), y) // 꼭지점
            path.lineTo(x, currentY) // Bottom right
            path.lineTo(currentX, currentY) // Bottom right
            path.close()
            canvas.drawPath(path, paint)
        }

        // 비트맵 이미지를 캔버스에 그려주는 메소드
        fun drawSketchBitmap(canvas: Canvas){
            val w: Int = sketch!!.getWidth()
            val h: Int = 850
            val src = Rect(0, 0, w, h)
            val dst = Rect((this.width-sketch!!.width)/2, 0, w+(this.width-sketch!!.width)/2, h)

            canvas.drawBitmap(sketch!!, src, dst, null)
        }



        private fun touch_start(x: Float, y: Float) {

            mPath.reset()
            path = Path_info()
            path.setpaint(mPaint)
            path_count = allpaths_info.size+1
            allpaths_info.add(path)

            if(zoomStatus){
                //확대했을때 그림을 그린 경우 좌표값을 축소했을때의 좌표값과 차이가 있어 계산해서 넣어줘야함
                var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                mPath.moveTo(listAbsolute.get(0), listAbsolute.get(1))
                allpaths_info.get(path_count-1).setxy(listAbsolute.get(0),listAbsolute.get(1))
            }else{
                mPath.moveTo(x, y)
                allpaths_info.get(path_count-1).setxy(x,y)
            }

            callprevpaint() // 전에 펜 상태 불러오기 ex) 색상

            if (penMode){
                allpaths_info.get(path_count-1).setpentype(pentype)
            }else if(shapeMode){
                allpaths_info.get(path_count-1).setshapetype(shapetype)
                if (zoomStatus){
                    var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                    allpaths_info.get(path_count-1).shapeRightX = listAbsolute.get(0)
                    allpaths_info.get(path_count-1).shapeRightY = listAbsolute.get(1)
                }else{
                    allpaths_info.get(path_count-1).shapeRightX = x
                    allpaths_info.get(path_count-1).shapeRightY = y
                }
            }else if(eraseMode){
                allpaths_info.get(path_count-1).erasestrokewidth = erasestrokewidth
                allpaths_info.get(path_count-1).setpenwidth(erasestrokewidth)
                allpaths_info.get(path_count-1).setpentype(4)
            }

        }

        private fun touch_move(x: Float, y: Float) {
            var count = path_count-countUndoWhenDrawing-1
            if(penMode || eraseMode){
                if(zoomStatus){
                    //확대 했을때 그림을 그린 경우 좌표값을 축소 했을때의 좌표값과 차이가 있어 계산해서 넣어줘야함
                    var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                    mPath.lineTo(listAbsolute.get(0), listAbsolute.get(1))
                    allpaths_info.get(count).setpath(mPath)
                    allpaths_info.get(count).setxy(listAbsolute.get(0),listAbsolute.get(1))
                }else{
                    mPath.lineTo(x, y)
                    allpaths_info.get(count).setpath(mPath)
                    allpaths_info.get(count).setxy(x,y)
                }

            }else if(shapeMode){
                if(zoomStatus){
                    var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                    allpaths_info.get(count).shapeRightX = listAbsolute.get(0)
                    allpaths_info.get(count).shapeRightY = listAbsolute.get(1)
                }else{
                    allpaths_info.get(count).shapeRightX = x
                    allpaths_info.get(count).shapeRightY = y
                }
            }
        }

        private fun touch_up(x: Float, y: Float) {
            var count = path_count-countUndoWhenDrawing-1

            allpaths_info.get(count).setname(name)

            WhiteBoardService.path_info =  allpaths_info.get(count)
            var msg: Message? = null
            msg = Message.obtain(null, WhiteBoardService.MSG_SEND_DRAWING)
            sendHandlerMessage(msg)

            countUndoWhenDrawing = 0
            mPath = Path()
            mPaint = Paint()
        }

        // 전에 그리던 펜 상태 값을 불러온다.
        fun callprevpaint(){
            mPaint.isAntiAlias = true
            mPaint.color = prevColor
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeJoin = Paint.Join.ROUND
            mPaint.strokeCap = Paint.Cap.ROUND
            mPaint.strokeWidth = prevWidth
            allpaths_info.get(path_count-1).setpencolor(prevColor)
            allpaths_info.get(path_count-1).setpenwidth(prevWidth)
        }

        fun setPaintColor(color : Int){
            mPaint.color = color
            prevColor = color
        }

        // 모두 지우기
        fun clearAll(){
            allpaths_info.clear()
            path_count = 0
            invalidate()
        }

        // 그림 저장
        fun save(context: Context){
            val saveFile = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(saveFile)
            draw(canvas)

            var dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!dir.exists()){
                dir.mkdir()
            }
            try {
                val fos: FileOutputStream = FileOutputStream(
                    File(dir, FILE_NAME+"_"+java.text.SimpleDateFormat("yyyyMMddHHmmss").format(Date())+".png"
                    )
                )

                saveFile.compress(Bitmap.CompressFormat.PNG, 100, fos)

                fos.close()
                Toast.makeText(context,"그림이 저장되었습니다.",Toast.LENGTH_LONG).show()
                //갤러리 갱신
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                    )
                )

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // 그리기 굵기 저장
        fun setStrokeWidth(width : Float){
            mPaint.strokeWidth = width
            prevWidth = width
        }

        // 지우개 굵기 저장
        fun setEraseStrokeWidth(width : Float){
            erasestrokewidth  = width
        }

        // 지우개 모드 on
        fun setEraseMode(){
            eraseMode = true
            shapeMode = false
            penMode = false
            pentype = 4
            shapetype = 0
            viewDataBinding.drawingpanel = this
        }

        // 도형 그리기 모드 on
        fun setShapeMode(type:Int){
            this.shapetype = type
            shapeMode = true
            penMode = false
            eraseMode = false
            pentype = 0
            viewDataBinding.drawingpanel = this

        }

        // 펜 모드 on
        fun setPenMode(type : Int){
            pentype = type
            shapeMode = false
            penMode = true
            eraseMode = false
            shapetype = 0
            drawingPanel_checksize.setPenType(type)
            viewDataBinding.drawingpanel = this
        }

        // 확대 모드 on
        fun setZoomMode(){
            zoomdirection = 1
            zoomStatus = true
            invalidate()
        }

        // 축소 모드 on
        fun setReleaseZoomMode(){
            zoomStatus = false
            zoomdirection = 0
            zoomDrawCount = 0
            invalidate()
        }

        // 확대 방향에 따라 zoomdirection에 해당 값을 저장한다.
        // ex) 1사분면 -> 1 2사분면 -> 2
        fun setZoomDirection(direction : String){
            when(direction){
                "left" -> {
                    if(zoomdirection == 2 || zoomdirection == 4){
                        zoomdirection--
                    }
                }
                "right" ->{
                    if(zoomdirection == 1 || zoomdirection ==3){
                        zoomdirection++
                    }
                }
                "top"->{
                    if(zoomdirection == 3 || zoomdirection ==4) {
                        zoomdirection = zoomdirection-2
                    }

                }
                "bottom" ->{
                    if(zoomdirection == 1 || zoomdirection ==2) {
                        zoomdirection = zoomdirection+2
                    }
                }
            }

            zoomStatus = true
            invalidate()
        }


        // 밑그림 이미지 저장
        fun setSketchImg(img : Bitmap){
            // 원본이미지 영역을 축소해서 그리기
            this.sketch = img
            invalidate()
        }

        // 살행 취소
        fun undo(){
            if(allpaths_info.size>0){
                for (i in allpaths_info.size-1 downTo 0){

                    if(allpaths_info.get(i).name.equals(name)){
                        undonePaths.add(allpaths_info.removeAt(i))
                        break
                    }
                }

                var msg: Message? = null
                msg = Message.obtain(null, WhiteBoardService.MSG_SEND_UNDO_ACTION)

                sendHandlerMessage(msg)
                invalidate()
            }
        }

        // 앞으로 가기
        fun redo(){
            if (undonePaths.size > 0) {
                allpaths_info.add(undonePaths.removeAt(undonePaths.size - 1))
                WhiteBoardService.path_info = allpaths_info.get(allpaths_info.size - 1)

                invalidate()

                var msg: Message? = null
                msg = Message.obtain(null, WhiteBoardService.MSG_SEND_DRAWING)

                sendHandlerMessage(msg)
            }
        }


        // 모든 메뉴 숨기기
        fun hideMenu(){
            viewDataBinding.dialogShapeSelect.visibility = View.INVISIBLE
            viewDataBinding.dialogPenSelect.visibility = View.INVISIBLE
            viewDataBinding.dialogColorpicker.visibility = View.INVISIBLE
            viewDataBinding.dialogEraseSelect.visibility = View.INVISIBLE

            layout.visibility = View.GONE
        }

        // 확대를 한 후 확대된 방향에 따라 현재 그리고 있는 좌표값들을 확대된 비율, 방향에따라 다시 계산준다.
        fun getAbsolutePosition(Ax: Float, Ay: Float, centerX : Float, centerY : Float, mScaleFactor : Float): FloatArray {
            var cx = 0f
            var cy = 0f
            when(zoomdirection){
                1->{
                    cx = 0f
                    cy = 0f
                }
                2->{
                    cx = this.width.toFloat()
                    cy = 0f
                }
                3->{
                    cx = 0f
                    cy = this.height.toFloat()
                }
                4->{
                    cx = this.width.toFloat()
                    cy = this.height.toFloat()
                }
            }
            val x: Float = getAbsolutePosition(cx, Ax,mScaleFactor)
            val y: Float = getAbsolutePosition(cy, Ay,mScaleFactor)
            return floatArrayOf(x, y)
        }

        private fun getAbsolutePosition(
            oldCenter: Float,
            newCenter: Float,
            mScaleFactor: Float
        ): Float {
            return if (newCenter > oldCenter) {
                oldCenter + (newCenter - oldCenter) / mScaleFactor
            } else {
                oldCenter - (oldCenter - newCenter) / mScaleFactor
            }
        }

        // 터치 이벤트
        override fun onTouch(arg0: View, event: MotionEvent): Boolean {
            hideMenu()
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touch_start(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    touch_move(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    touch_up(x,y)
                    invalidate()
                }
            }
            return true
        }

        init {
            isFocusable = true
            isFocusableInTouchMode = true
            setOnTouchListener(this)
            mPaint = Paint()
            outercirclePaint = Paint()
            outercirclePaint.isAntiAlias = true
            mPaint.isAntiAlias = true
            mPaint.color = Color.BLACK
            outercirclePaint.color = 0x44FFFFFF
            outercirclePaint.style = Paint.Style.STROKE
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeJoin = Paint.Join.ROUND
            mPaint.strokeCap = Paint.Cap.ROUND
            mPaint.strokeWidth = 10f
            setPenMode(1)

            outercirclePaint.strokeWidth = 6f
            mCanvas = Canvas()
            mPath = Path()
        }
    }

    inner class DrawingPanel_CheckWidth(context: Context?) : View(context){

        private val mCanvas: Canvas
        private var mPath: Path
        private var mPaint: Paint
        var path = Path_info()
        var pentype : Int = 1
        var sketch : Bitmap? = null

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            mPath.moveTo(this.width.toFloat()*1/10,(this.height/2).toFloat()) // Bottom left
            mPath.lineTo(this.width.toFloat()*9/10,(this.height/2).toFloat()) // Bottom right

            canvas!!.drawPath(mPath, mPaint)
        }

        fun setSize(size : Float){
            mPaint.strokeWidth = size
            invalidate()
        }

        fun setPenType(type : Int){
            when(type){
                1 -> {
                }
                2-> {           // 형광펜
                    mPaint.alpha = 125
                }
                3 -> {            // 붓
                    mPaint.alpha = 25
                }

            }
            invalidate()
        }

        lateinit var stroke : Stroke
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.color = Color.BLACK
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeJoin = Paint.Join.ROUND
            mPaint.strokeCap = Paint.Cap.ROUND
            mPaint.strokeWidth = 10f
            mCanvas = Canvas()
            mPath = Path()

        }
    }



}