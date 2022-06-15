package com.coworkerteam.coworker.ui.unity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.constraintlayout.widget.ConstraintLayout
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.databinding.ActivityWhiteboardBinding
import com.coworkerteam.coworker.ui.base.BaseActivity
import com.coworkerteam.coworker.ui.camstudy.enter.EnterCamstudyViewModel
import com.coworkerteam.coworker.ui.dialog.SketchChoiceDialog
import com.coworkerteam.coworker.ui.unity.data.Path_info
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class WhiteBoardActivity : BaseActivity<ActivityWhiteboardBinding, EnterCamstudyViewModel>() {

    val TAG = "WhiteBoardActivity"

    override val layoutResourceID: Int
        get() = R.layout.activity_whiteboard
    override val viewModel: EnterCamstudyViewModel by viewModel()
    var canvas: ConstraintLayout? = null
    var canvas_checkwidth: LinearLayout? = null

    var menu : Menu? = null
    private val undonePaths = ArrayList<Path>()
    private val paths_circle = ArrayList<Path>()
    private val paths = ArrayList<Path>()
    private val paths_info = ArrayList<Path_info>()

    lateinit var drawingPanel : DrawingPanel
    lateinit var drawingPanel_checksize : DrawingPanel_CheckWidth

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    val FILE_NAME = "studyday"

    var nowcolor = 0

    companion object {
        private const val TOUCH_TOLERANCE = 0f
    }

    override fun initStartView() {
        canvas_checkwidth = findViewById<View>(R.id.whiteboard_canvas_check_width) as LinearLayout
        drawingPanel_checksize = DrawingPanel_CheckWidth(this)
        canvas_checkwidth!!.addView(drawingPanel_checksize)

        canvas = findViewById<View>(R.id.whiteboard_canvas) as ConstraintLayout
        drawingPanel = DrawingPanel(this)
        canvas!!.addView(drawingPanel)
        viewDataBinding.activitiy = this
        viewDataBinding.drawingpanel = drawingPanel
        viewDataBinding.zoomdirection = drawingPanel.zoomdirection



        //툴바 세팅
        var main_toolbar = viewDataBinding.toolbarWhiteboard as androidx.appcompat.widget.Toolbar

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24_write) // 홈버튼 이미지 변경

        setSupportActionBar(main_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(false) // 드로어를 꺼낼 홈 버튼 활성화

        var btn_undo = findViewById<ImageView>(R.id.whiteboard_toolbar_undo)
        var btn_redo = findViewById<ImageView>(R.id.whiteboard_toolbar_redo)
        var btn_zoom = findViewById<ImageView>(R.id.whiteboard_toolbar_zoom)

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


        viewDataBinding.seekBarStrokewidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d(TAG,"PRGRESS"+progress)
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
                Log.d(TAG,"PRGRESS"+progress)
                drawingPanel.setEraseStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        /*
        val brightnessSlideBar = findViewById<BrightnessSlideBar>(R.id.brightnessSlide)
        viewDataBinding.dialogColorpickerPallete.attachBrightnessSlider(brightnessSlideBar)
        viewDataBinding.dialogColorpickerPallete.setInitialColor(Color.BLACK)
        viewDataBinding.dialogColorpickerPallete.setColorListener(ColorListener(){ color: Int, b: Boolean ->
            drawingPanel.setPaintColor(color)
        })
         */
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
                       drawingPanel.save(this@WhiteBoardActivity)
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        // 권한 거부시 실행  할 내용
                        Toast.makeText(this@WhiteBoardActivity,"권한을 허용하지 않으면 그림을 저장 할 수 없습니다..",Toast.LENGTH_SHORT).show()

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
                finish()
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


    fun SetSketchURL(url : String){
        var imgUrl : URL
		var connection : HttpURLConnection
		var iS : InputStream
		var sketchBitmap : Bitmap? = null

        coroutineScope.launch {
            val originalDeferred = coroutineScope.async(Dispatchers.IO) {
                getOriginalBitmap(url)
            }
             sketchBitmap = originalDeferred.await()
            sketchBitmap?.let { drawingPanel.setSketchImg(it) }
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
        setVisible(View.VISIBLE,viewDataBinding.dialogColorpicker)
        drawingPanel.setPaintColor(viewDataBinding.dialogColorpickerPallete.color)
    }

    fun changeZoomDirection(direction: String){
        Log.d(TAG,"DIRECTION"+direction)
        drawingPanel.setZoomDirection(direction)
        viewDataBinding.zoomdirection = drawingPanel.zoomdirection
    }

    fun showSketchMenu(){

        drawingPanel.hideMenu()

        SketchChoiceDialog.Builder(this).show()
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
            drawingPanel.clearAll()
            builder.dismiss()
        })

        btn_cancle.setOnClickListener(View.OnClickListener {
            builder.dismiss()
        })
    }

    fun setVisible(visible :Int,view: View){
        view.visibility = if(visible == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }



    inner class DrawingPanel(context: Context?) : View(context),
        View.OnTouchListener {
        private val mCanvas: Canvas
        private var mPath: Path
        private var mPaint: Paint
        private val outercirclePaint: Paint
        var path = Path_info()
        var path_count = 0
        // private ArrayList<Path> undonePaths = new ArrayList<Path>();
        var prevColor : Int = Color.BLACK
        var prevWidth : Float = 50f

        var penMode : Boolean = false
        var pentype : Int = 1

        var sketch : Bitmap? = null

        private val undonePaths = ArrayList<Path_info>()

        var shapeMode : Boolean = false
        var shapetype : Int = 0

        var eraseMode : Boolean =false

        lateinit var stroke : Stroke

        var erasestrokewidth : Float = 50f
        // 확대관련 변수
        var zoomStatus = false
        var zoomDrawCount = 0
        var zoomdirection = 0

        fun colorChanged(color: Int) {
            mPaint.color = color
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
        }

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
            Log.d(TAG,"paths_info"+paths_info.size)
          for (i in 0..paths_info.size-1) {
              var p = paths_info.get(i)
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
                          drawTriangle(canvas, p.paint, p.listxy.get(0).x, p.listxy.get(0).y, p.shapeRightX - p.listxy.get(0).x);
                      }
                      3->{
                          var rect =Rect(p.listxy.get(0).x.toInt(),p.listxy.get(0).y.toInt(),
                              p.shapeRightX.toInt(), p.shapeRightY.toInt()
                          )
                          Log.d(TAG,"X: " +p.listxy.get(0).x.toInt() +"SX"+ p.shapeRightX.toInt())
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

        private var mX = 0f
        private var mY = 0f

        fun drawSketchBitmap(canvas: Canvas){
            val w: Int = sketch!!.getWidth()
            val h: Int = 850
            Log.d(TAG,"v"+w +"h"+h)
            val src = Rect(0, 0, w, h)
            val dst = Rect((this.width-sketch!!.width)/2, 0, w+(this.width-sketch!!.width)/2, h)

            canvas.drawBitmap(sketch!!, src, dst, null)
        }



        private fun touch_start(x: Float, y: Float) {
            mPath.reset()

            path = Path_info()
            path.setpaint(mPaint)
            path_count++
            paths_info.add(path)

            if(zoomStatus){
                //확대했을때 그림을 그린 경우 좌표값을 축소했을때의 좌표값과 차이가 있어 계산해서 넣어줘야함
                var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                mPath.moveTo(listAbsolute.get(0), listAbsolute.get(1))
                paths_info.get(path_count-1).setxy(listAbsolute.get(0),listAbsolute.get(1))
            }else{
                mPath.moveTo(x, y)
                paths_info.get(path_count-1).setxy(x,y)
            }


            callprevpaint() // 전에 펜 상태 불러오기 ex) 색상

            if (penMode){
                Log.d(TAG,"pentype"+pentype)
                paths_info.get(path_count-1).setpentype(pentype)
            }else if(shapeMode){
                paths_info.get(path_count-1).setshapetype(shapetype)
                if (zoomStatus){
                    var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                    paths_info.get(path_count-1).shapeRightX = listAbsolute.get(0)
                    paths_info.get(path_count-1).shapeRightY = listAbsolute.get(1)
                }else{
                    paths_info.get(path_count-1).shapeRightX = x
                    paths_info.get(path_count-1).shapeRightY = y
                }


            }else if(eraseMode){
                paths_info.get(path_count-1).erasestrokewidth = erasestrokewidth
                paths_info.get(path_count-1).setpentype(4)
                Log.d(TAG,"STROKEWIDTH"+erasestrokewidth)
            }

        }

        private fun touch_move(x: Float, y: Float) {
            if(penMode || eraseMode){
                if(zoomStatus){
                    //확대 했을때 그림을 그린 경우 좌표값을 축소 했을때의 좌표값과 차이가 있어 계산해서 넣어줘야함
                    var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                    mPath.lineTo(listAbsolute.get(0), listAbsolute.get(1))
                    paths_info.get(path_count-1).setpath(mPath)
                    paths_info.get(path_count-1).setxy(listAbsolute.get(0),listAbsolute.get(1))
                }else{
                    mPath.lineTo(x, y)
                    paths_info.get(path_count-1).setpath(mPath)
                    paths_info.get(path_count-1).setxy(x,y)
                }

            }else if(shapeMode){
                if(zoomStatus){
                    var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                    paths_info.get(path_count-1).shapeRightX = listAbsolute.get(0)
                    paths_info.get(path_count-1).shapeRightY = listAbsolute.get(1)
                }else{
                    paths_info.get(path_count-1).shapeRightX = x
                    paths_info.get(path_count-1).shapeRightY = y
                }
            }

            /*
            if (zoomStatus){
                // 확대했을 경우 내가 그릴때 보여지는 좌표값
                var listAbsolute = getAbsolutePosition(x,y,0f,0f,2f)
                paths_info.get(path_count-1).setzoomxy(listAbsolute.get(0),listAbsolute.get(1))
            }

             */
        }

        private fun touch_up(x: Float, y: Float) {
            if(penMode || eraseMode){
                paths_info.get(path_count-1).setpath(mPath)
            }

            mPath = Path()
            mPaint = Paint()

            Log.d(TAG,paths_info.toString())
        }

        fun callprevpaint(){
            mPaint.isAntiAlias = true
            mPaint.color = prevColor
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeJoin = Paint.Join.ROUND
            mPaint.strokeCap = Paint.Cap.ROUND
            mPaint.strokeWidth = prevWidth
        }

        fun setPaintColor(color : Int){
            mPaint.color = color
            prevColor = color
        }

        fun clearAll(){
            paths_info.clear()
            path_count = 0
            invalidate()
        }

        fun save(context: Context){
            val saveFile = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(saveFile)
            draw(canvas)

            var dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!dir.exists()){
                dir.mkdir()
            }
            Log.d("Paintview dir : ",dir.absolutePath)
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

        fun setStrokeWidth(width : Float){
            mPaint.strokeWidth = width
            prevWidth = width
        }

        fun setEraseStrokeWidth(width : Float){
            erasestrokewidth  = width
        }

        fun setEraseMode(){
            eraseMode = true
            shapeMode = false
            penMode = false
            pentype = 4
            viewDataBinding.drawingpanel = this
        }

        fun setShapeMode(type:Int){
            this.shapetype = type
            shapeMode = true
            penMode = false
            eraseMode = false
            pentype = 0
            viewDataBinding.drawingpanel = this

        }

        fun setPenMode(type : Int){
            pentype = type
            shapeMode = false
            penMode = true
            eraseMode = false
            drawingPanel_checksize.setPenType(type)
            viewDataBinding.drawingpanel = this
            viewDataBinding.drawingpanel = this
        }


        fun setZoomMode(){
            zoomdirection = 1
            zoomStatus = true
            invalidate()
        }

        fun setReleaseZoomMode(){
            zoomStatus = false
            zoomdirection = 0
            zoomDrawCount = 0
            invalidate()
        }

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


        fun setSketchImg(img : Bitmap){
            // 원본이미지 영역을 축소해서 그리기
            this.sketch = img
           invalidate()
        }

        fun undo(){
            if(paths_info.size>0){
                undonePaths.add(paths_info.removeAt(paths_info.size-1))
                path_count--
                invalidate()
            }
        }

        fun redo(){
            if (undonePaths.size > 0) {
                paths_info.add(undonePaths.removeAt(undonePaths.size - 1))
                path_count++
                invalidate()
            }
        }




        fun hideMenu(){
            viewDataBinding.dialogShapeSelect.visibility = View.INVISIBLE
            viewDataBinding.dialogPenSelect.visibility = View.INVISIBLE
           viewDataBinding.dialogColorpicker.visibility = View.INVISIBLE
            viewDataBinding.dialogEraseSelect.visibility = View.INVISIBLE
        }

        fun drawTriangle(canvas : Canvas,  paint : Paint, x : Float,  y : Float, width :Float){
            val halfWidth = width / 2
            val path = Path()
            Log.d("width",width.toString())
            path.moveTo(x, (y + width).toFloat()) // Bottom left
            path.lineTo(x+halfWidth, y) // Bottom right
            path.lineTo((x + width).toFloat(), (y + width).toFloat()) // Bottom right
           // path.lineTo(x.toFloat(), (y - halfWidth).toFloat()) // Back to Top
            path.close()
            canvas.drawPath(path, paint)
        }

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
                    Log.d(TAG,"X : " + x + "Y"+y)
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
            mPaint.strokeWidth = 50f
            //  mPaint.alpha = 130
            setPenMode(1)

            outercirclePaint.strokeWidth = 6f
            mCanvas = Canvas()
            mPath = Path()
            paths.add(mPath)
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

            Log.d(TAG,"WIDTH"+canvas!!.width)
            Log.d(TAG,"height"+canvas!!.height)

            mPath.moveTo(this.width.toFloat()*1/7f,(this.height/2).toFloat()) // Bottom left
            mPath.lineTo(this.width.toFloat()*6/7,(this.height/2).toFloat()) // Bottom right

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
            mPaint.strokeWidth = 50f
            //  mPaint.alpha = 130

            mCanvas = Canvas()
            mPath = Path()





        }
    }

}