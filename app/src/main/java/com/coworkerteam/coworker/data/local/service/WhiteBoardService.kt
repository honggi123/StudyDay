package com.coworkerteam.coworker.data.local.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.unity.data.Path_info
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.EngineIOException
import org.json.JSONException
import org.json.JSONObject

class WhiteBoardService : Service() {
    val TAG = "WhiteBoardService"

    val NOTIFICATION_ID = 1005

    var socket: Socket? = null

    lateinit var hostname: String
    lateinit var room: String
    var instance: String? = null

    var speakStatus : Boolean = false
    var noiseStatus : Boolean = false
    var gson : Gson = Gson()
    var myname = ""
    var roomLink = ""

    companion object {
        const val MSG_CLIENT_CONNECT = 0
        const val MSG_CLIENT_DISCNNECT = 1
        const val MSG_SERVICE_CONNECT = 2
        const val MSG_SEND_DRAWING = 3
        const val MSG_SEND_SKETCH = 4
        const val MSG_SEND_UNDO_ACTION = 5
        const val MSG_SEND_REMOVE_ACTION = 6
        const val MSG_LEAVE_ROOM = 7
        const val MSG_RECEIVE_ROOM_DATA = 8
        const val MSG_RECEIVE_DRAWING= 9
        const val MSG_RECEIVE_SKETCH= 10
        const val MSG_RECEIVE_ACTION= 11
        const val MSG_RECEIVE_LEAVE= 12
        const val MSG_RECEIVE_INTO= 13

        lateinit var path_info : Path_info
         var sketchNum : Int = 0
    }


    private var mClientCallbacks = ArrayList<Messenger>()
    val mMessenger = Messenger(CallbackHandler(Looper.getMainLooper()))


    //서비스가 시작될 때 호출
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"service 시작")
        myname = intent!!.getStringExtra("name").toString()
        roomLink = intent!!.getStringExtra("roomLink").toString()
        if (socket == null) {
            Log.d(TAG,"service 시작")
            val notification = notification_whiteboard.createNotification(this)
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }


    //bindService()로 바인딩을 실행할 때 호출
    override fun onBind(intent: Intent): IBinder {
        if (socket == null) {
        }
        return mMessenger.binder
    }

    //unbindService()로 바인딩을 해제할 때 호출
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.d(TAG,"onUnbind")
    }

    //이미 onUnbind()가 호출된 후에 bindService()로 바인딩을 실행할 때 호출
    override fun onRebind(intent: Intent?){
        super.onRebind(intent)
    }

    //서비스가 소멸될 때 호출
    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        socket!!.disconnect()
        super.onDestroy()
    }



    private fun connectToServer() {
        // 소켓 서버 연결
        socket = IO.socket("https://testcamstudy.shop")
        socket!!.connect()
        socket!!.on(io.socket.client.Socket.EVENT_CONNECT) {
            // 소켓 서버에 연결이 성공하면 호출됩니다.
            Log.i("Socket", "Connect")
        }.on(io.socket.client.Socket.EVENT_DISCONNECT) { args ->
            // 소켓 서버 연결이 끊어질 경우에 호출됩니다.
            Log.i("Socket", "Disconnet: ${args[0]}")
        }.on(Manager.EVENT_CONNECT_ERROR) { args ->
            // 소켓 서버 연결 시 오류가 발생할 경우에 호출됩니다.
            var errorMessage = ""
            if (args[0] is EngineIOException) {
                //errorMessage = "code: ${err.code}  message: ${err.message}"
            }
            Log.i("Socket", "Connect Error: $errorMessage")
        }.on(Socket.EVENT_RECONNECT,
            Emitter.Listener { args: Array<Any> ->
                //재연결 처리
            })
            .on(
                Socket.EVENT_CONNECT_ERROR,
                Emitter.Listener { args: Array<Any> ->
                    Log.d(TAG, "소켓연결 에러")
                }).on("message",
                Emitter.Listener { args: Array<Any> ->
                    try {
                        if (args[0] is String) {
                        } else {
                            val message = args[0] as JSONObject
                            Log.d(TAG,"소켓 메시지 도착 : "+message)
                            when (message.getString("id")) {
                                "reconnect failed" -> {
                                    //재연결 처리 실패
                                    Log.d(TAG, "reconnect failed")
                                    val handlerMessage = Message.obtain(
                                        null,
                                        CamStudyService.MSG_COMSTUDY_LEFT
                                    )
                                    // mMessenger.send(handlerMessage)
                                }
                                "setWhiteboard" -> {
                                    val handlerMessage =
                                        Message.obtain(null, MSG_RECEIVE_ROOM_DATA)

                                    var bundle = Bundle()
                                    bundle.putString("data",message.toString())

                                    handlerMessage.data = bundle
                                    sendHandlerMessage(handlerMessage)

                                }"receiveDraw" -> {
                                        val handlerMessage =
                                            Message.obtain(null, MSG_RECEIVE_DRAWING)

                                        var bundle = Bundle()
                                        bundle.putString("data",message.getString("draw"))

                                        handlerMessage.data = bundle
                                        sendHandlerMessage(handlerMessage)
                                }"receiveSetSketch"->{
                                    val handlerMessage =
                                        Message.obtain(null, MSG_RECEIVE_SKETCH)

                                    var bundle = Bundle()
                                    bundle.putString("data",message.toString())

                                    handlerMessage.data = bundle
                                    sendHandlerMessage(handlerMessage)
                                }"receiveCanvasAction"->{
                                    val handlerMessage =
                                        Message.obtain(null, MSG_RECEIVE_ACTION)

                                    var bundle = Bundle()
                                    bundle.putString("data",message.toString())

                                    handlerMessage.data = bundle
                                    sendHandlerMessage(handlerMessage)
                                }"intoParticipant"->{
                                    val handlerMessage =
                                        Message.obtain(null, MSG_RECEIVE_INTO)

                                    var bundle = Bundle()
                                    bundle.putString("data",message.toString())

                                    handlerMessage.data = bundle
                                    sendHandlerMessage(handlerMessage)

                                }"leaveWhiteboard"-> {
                                    val handlerMessage =
                                        Message.obtain(null, MSG_RECEIVE_LEAVE)

                                    var bundle = Bundle()
                                    bundle.putString("data", message.toString())

                                    handlerMessage.data = bundle
                                    sendHandlerMessage(handlerMessage)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }).on(io.socket.client.Socket.EVENT_DISCONNECT,
                Emitter.Listener { args: Array<Any?>? ->
                    Log.d(
                        TAG,
                        "connectToServer: disconnect"
                    )
                })
    }



    private fun sendMessage(message: JSONObject){
        try {
            Log.d(TAG, "sendMessage: $message")
            socket!!.emit("message", message)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }



    private fun sendHandlerMessage(msg: Message) {
        if (mClientCallbacks.size > 0) {
            try {
                //bind된 Activicy들에게 메시지 전송
                mClientCallbacks.forEach {
                    val snedMsg = Message()
                    snedMsg.copyFrom(msg)
                    it.send(snedMsg)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            Log.d(TAG, "Send MSG_ADD_VALUE message to Service ")
        }
    }

    inner class CallbackHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, "Service에 메세지 도착")
            when (msg.what) {
                MSG_CLIENT_CONNECT -> {
                    Log.d(TAG, "Received MSG_CLIENT_CONNECT message from client");
                    mClientCallbacks.add(msg.replyTo);
                    connectToServer()

                    val message = JSONObject()

                    message.put("id", "intoCanvas")
                    message.put("roomLink","https://www.studyday.co.kr/link?idx=211?pwd=null")
                    message.put("nickname","honghong5")

                    sendMessage(message)

                    val handlerMessage =
                        Message.obtain(null, MSG_SERVICE_CONNECT)
                    sendHandlerMessage(handlerMessage)
                }
                MSG_CLIENT_DISCNNECT -> {
                    Log.d(TAG, "Received MSG_CLIENT_DISCONNECT message from client");
                    mClientCallbacks.remove(msg.replyTo);
                }
                MSG_SEND_DRAWING -> {
                    Log.d(TAG,"MSG_SEND_DRAWING")

                    val message = JSONObject()

                    message.put("id", "sendDraw")
                    message.put("nickname","honghong5")
                    var drawData : String = gson.toJson(path_info)
                    //  list.put(drawData)
                    message.put("draw",drawData)
                    var s = message.toString().replace("\\", "")

                    Log.d(TAG,"message : "+ message.toString())
                   sendMessage(JSONObject(message.toString()))
                  // sendMessage(getSendMessage("drawing"))
                }MSG_SEND_SKETCH -> {
                    val message = JSONObject()

                    message.put("id", "sendSetSketch")
                    message.put("nickname","honghong5")
                    message.put("sketchNum",msg.arg1)

                    sendMessage(message)
                }MSG_SEND_UNDO_ACTION ->{
                    val message = JSONObject()

                    message.put("id", "sendCanvasAction")
                    message.put("nickname","honghong5")
                    message.put("actionName","undo")

                    sendMessage(message)
                }MSG_SEND_REMOVE_ACTION-> {

                val message = JSONObject()

                message.put("id", "sendCanvasAction")
                message.put("nickname","honghong5")
                message.put("actionName","remove")

                sendMessage(message)

                val handlerMessage =
                    Message.obtain(null, MSG_SEND_REMOVE_ACTION)
                sendHandlerMessage(handlerMessage)

                }MSG_LEAVE_ROOM  -> {
                    val message = JSONObject()

                    message.put("id", "leaveWhiteboard")
                    message.put("nickname","honghong5")

                    sendMessage(message)
                }

                }

            }
        }
    }




object notification_whiteboard {
    const val CHANNEL_ID = "foreground_service_channel" // 임의의 채널 ID
    fun createNotification(
        context: Context
    ): Notification {
        // 알림 클릭시 MainActivity로 이동됨
        /*
        val notificationIntent = Intent(context, CamStudyActivity::class.java)
        notificationIntent.putExtra("audio", CamStudyService.isAudio)
        notificationIntent.putExtra("video", CamStudyService.isVideo)
        notificationIntent.putExtra("studyInfo", CamStudyActivity.studyInfo)
        notificationIntent.putExtra("timer", CamStudyService.timer)
//      notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent
            .getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
*/
        // Oreo 부터는 Notification Chan3nel을 만들어야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "StudyDay", // 채널표시명
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }

        // 알림
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("StudyDay")
            .setContentText("화이트보드 진행중")
            .setSmallIcon(R.mipmap.ic_studyday)
            .setOngoing(true) // true 일경우 알림 리스트에서 클릭하거나 좌우로 드래그해도 사라지지 않음
            //  .setContentIntent(pendingIntent)
            .build()

        return notification
    }



}


