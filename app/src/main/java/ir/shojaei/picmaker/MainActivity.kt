package ir.shojaei.picmaker

import android.Manifest
import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayoutMediator
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import ir.shojaei.picmaker.databinding.ActivityMainBinding
import ir.shojaei.picmaker.databinding.DialogColorpickerBinding
import ir.shojaei.picmaker.databinding.DialogWaitForSaveBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var dialog:DialogPlus

    private lateinit var getCroppedImageLauncher: ActivityResultLauncher<CropImageContractOptions>

    private lateinit var imageRequest:ActivityResultLauncher<String>
    private lateinit var permissionRequest:ActivityResultLauncher<String>

    private var originalImageUri: Uri?=null

    private var currentColor=Color.BLACK

    private var lastId=-1
    private var selectedId=-1

    private var isColorPicker=false
    private var pixelColor = 0

    private lateinit var colorPickerBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var drawerBottomSheetBehavior: BottomSheetBehavior<View>

    private val colors=Array(2){ Color.WHITE }

    private lateinit var imageUtils: ImageUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()
        setupLaunchers()
        startApp()
    }

    private fun setupLaunchers(){
        getCroppedImageLauncher=registerForActivityResult(CropImageContract()){
            if (it.isSuccessful){
                originalImageUri=it.uriContent!!
                binding.mainLayout.visibility=GONE
                binding.vgCanvas.visibility= VISIBLE
                binding.saveBtn.visibility= VISIBLE
                binding.imagebackground.setBackgroundColor(Color.TRANSPARENT)
                binding.imagebackground.setImageURI(originalImageUri)
            }
        }

        imageRequest=registerForActivityResult(ActivityResultContracts.GetContent()){
            it?.let {
                val ins=contentResolver.openInputStream(it)
                imageUtils.addImage(
                    ++lastId,
                    BitmapFactory.decodeStream(ins)
                ){
                    selectedId=it
                    selectView()
                }
                selectedId=lastId
            }
        }

        permissionRequest=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it) saveImage()
        }
    }

    private fun startApp(){
        val mAdapter=MViewPagerAdapter(this)
        binding.viewPager.adapter=mAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager){tab, i->
            tab.text=if (i==0) "متن" else "تصویر"
        }.attach()

        binding.mainLayout.setOnClickListener {
            showDialogPlus(R.layout.dialog_getimage, MyFlags.BACKGROUND)
        }
        imageUtils= ImageUtils(this, binding.vgCanvas)

        binding.vgCanvas.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    if (!isColorPicker){
                        selectedId=-1
                        selectView()
                    }
                }
                MotionEvent.ACTION_MOVE->{
                    if (isColorPicker){
                        try {
                            val bitmap=getFrameLayoutBitmap()
                            pixelColor=bitmap.getPixel(motionEvent.x.toInt(), motionEvent.y.toInt())
                            binding.showcolor.setBackgroundColor(pixelColor)
                        }catch (e:Exception){}
                    }
                }
            }
            true
        }

        binding.saveBtn.setOnClickListener {
            checkPermission()
        }

        colorPickerBottomSheetBehavior= BottomSheetBehavior.from(binding.getcolorDialog)
        colorPickerBottomSheetBehavior.addBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_COLLAPSED-> {
                        isColorPicker = false
                        binding.showcolor.setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, R.color.colorPrimary)
                        )
                    }
                    BottomSheetBehavior.STATE_EXPANDED->isColorPicker=true
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })

        drawerBottomSheetBehavior= BottomSheetBehavior.from(binding.drawDialog)
        drawerBottomSheetBehavior.addBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_COLLAPSED-> {
                        binding.drawView.setInactive()
                        binding.drawView.inactiveEraser()
                        binding.setDrawingBrushBtn.setBackgroundResource(R.drawable.drawer_active_item_background)
                        binding.setDrawingEraserBtn.setBackgroundColor(Color.TRANSPARENT)
                    }
                    BottomSheetBehavior.STATE_EXPANDED->{
                        binding.drawView.setActive()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        })
    }

    private fun getFrameLayoutBitmap():Bitmap{
        val bitmap=Bitmap.createBitmap(binding.vgCanvas.width, binding.vgCanvas.height, Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap)
        binding.vgCanvas.draw(canvas)
        return bitmap
    }

    private fun checkPermission(){
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M until Build.VERSION_CODES.Q){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                permissionRequest.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }else saveImage()
        }else saveImage()
    }

    private fun saveImage(){
        val dialogBinding:DialogWaitForSaveBinding=DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_wait_for_save,
            null,
            false
        )

        val dialog=MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.show()

        selectedId=-1
        selectView()

        val bitmap=getFrameLayoutBitmap()

        CoroutineScope(Dispatchers.IO).launch {
            val calendar=Calendar.getInstance()
            val name="${calendar[Calendar.YEAR]}-${calendar[Calendar.MONTH]}-${calendar[Calendar.DAY_OF_MONTH]}" +
                    " ${calendar[Calendar.HOUR]}-${calendar[Calendar.MINUTE]}-${calendar[Calendar.SECOND]}"
            val imageCollection=
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                else
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val imageDetails=ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }
            val imageUri=contentResolver.insert(imageCollection, imageDetails)
            contentResolver.openOutputStream(imageUri!!).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, it)
            }
            withContext(Dispatchers.Main){
                dialog.dismiss()
            }
        }
    }

    fun showDialogPlus(layoutId:Int, flag: MyFlags?){
        dialog= DialogPlus.newDialog(this)
            .setContentHolder(ViewHolder(layoutId))
            .setCancelable(true)
            .setOnClickListener { _, view ->
                checkClickedItem(view.id, flag)
            }
            .setGravity(Gravity.BOTTOM)
            .create()
        dialog.show()

        when(layoutId){
            R.layout.dialog_textsize->textSize()
            R.layout.dialog_font->setFont(flag)
            R.layout.dialog_gradient->textGradient()
            R.layout.dialog_textrotate->textRotation()
            R.layout.dialog_textstroke->textStroke()
            R.layout.dialog_textshadow->textShadow()
            R.layout.dialog_opacity->{
                if (flag==null)textOpacity() else imageOpacity()
            }
            R.layout.dialog_effect->setFilters()
        }

        if (flag==MyFlags.EDIT_TEXT){
            findViewById<EditText>(R.id.text_et).setText(
                imageUtils.getText(selectedId)
            )
        }
    }

    //region text
    private fun textOpacity(){
        findViewById<Slider>(R.id.slider_opacity).apply {
            value=imageUtils.getTextOpacity(selectedId)
            addOnChangeListener { _, value, _ ->
                imageUtils.setTextOpacity(selectedId, value)
            }
        }
    }

    private fun textShadow(){
        findViewById<Slider>(R.id.slider_textshadow).apply {
            value=imageUtils.getShadow(selectedId)
            addOnChangeListener { _, value, _ ->
                imageUtils.setShadow(selectedId, value)
            }
        }
    }

    private fun textStroke(){
        findViewById<Slider>(R.id.slider_textstroke).apply {
            value=imageUtils.getStrokeSize(selectedId).toFloat()
            addOnChangeListener { _, value, _ ->
                imageUtils.getStrokeSize(selectedId, value.toInt())
            }
        }
    }

    private fun textRotation(){
        val values=imageUtils.getRotation(selectedId)

        val sliderX:Slider=findViewById(R.id.slider_rotatex)
        val sliderY:Slider=findViewById(R.id.slider_rotatey)
        val sliderXY:Slider=findViewById(R.id.slider_rotatexy)

        values?.let {
            sliderX.value=it[RotationFlag.ROTATION_X]?.toFloat()!!
            sliderY.value=it[RotationFlag.ROTATION_Y]?.toFloat()!!
            sliderXY.value=it[RotationFlag.ROTATION]?.toFloat()!!
        }

        sliderX.addOnChangeListener { _, value, _ ->
            imageUtils.setRotation(selectedId, value, RotationFlag.ROTATION_X)
        }
        sliderY.addOnChangeListener { _, value, _ ->
            imageUtils.setRotation(selectedId, value, RotationFlag.ROTATION_Y)
        }
        sliderXY.addOnChangeListener { _, value, _ ->
            imageUtils.setRotation(selectedId, value, RotationFlag.ROTATION)
        }
    }

    private fun textGradient(){
        findViewById<MaterialButton>(R.id.getcolor1).setOnClickListener {
            showColorPickerDialog(MyFlags.GRADIENT1)
        }
        findViewById<MaterialButton>(R.id.getcolor2).setOnClickListener {
            showColorPickerDialog(MyFlags.GRADIENT2)
        }

        val slider1:Slider=findViewById(R.id.sliderg1)
        val slider2:Slider=findViewById(R.id.sliderg2)
        val slider3:Slider=findViewById(R.id.sliderg3)

        val onSliderChange=
            Slider.OnChangeListener { _, _, _ ->
                imageUtils.setGradient(
                    selectedId,
                    listOf(slider1.value.toInt(),slider2.value.toInt(),slider3.value.toInt()),
                    colors.toList()
                )
            }

        slider1.addOnChangeListener(onSliderChange)
        slider2.addOnChangeListener(onSliderChange)
        slider3.addOnChangeListener(onSliderChange)
    }

    private fun setFont(flag: MyFlags?){
        val fList= mutableListOf<String>()
        val path=if (flag==MyFlags.PER_FONT)"per_fonts" else "eng_fonts"
        assets.list(path)?.forEach {
            fList.add("$path/$it")
        }
        val fAdapter=FontAdapter(imageUtils.getText(selectedId), fList){
            dialog.dismiss()
            imageUtils.setFont(selectedId, fList[it])
        }
        findViewById<RecyclerView>(R.id.listfont).apply { adapter=fAdapter }
    }

    private fun textSize(){
        findViewById<Slider>(R.id.slider_textsize).apply {
            value=imageUtils.getTextSize(selectedId).toFloat()
            addOnChangeListener { _, value, _ ->
                imageUtils.setTextSize(selectedId, value)
            }
        }
    }

    fun copyText(){
        imageUtils.copyText(selectedId, ++lastId){
            selectedId=it
            selectView()
        }
        selectedId=lastId
    }
    //endregion

    private fun setFilters(){
        originalImageUri?.let {
            val filterAdapter=FilterAdapter(
                it,
                imageUtils.getFilters()
            ){
                it?.let {filter->
                    Glide.with(this).load(originalImageUri).apply(filter).into(binding.imagebackground)
                }?: binding.imagebackground.setImageURI(originalImageUri)
            }
            findViewById<RecyclerView>(R.id.filter_recycler_view).apply { adapter=filterAdapter }
        }?: kotlin.run {
            Toast.makeText(this, "لطفا یک تصویر برای پس زمینه انتخاب کنید", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    fun addImage(){
        imageRequest.launch("image/*")
    }

    private fun imageOpacity(){
        findViewById<Slider>(R.id.slider_opacity).apply {
            value=imageUtils.getImageOpacity(selectedId)
            addOnChangeListener { _, value, _ ->
                imageUtils.setImageOpacity(selectedId, value)
            }
        }
    }

    fun startColorPicker(){
        colorPickerBottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED
        binding.setTextColorBtn.setOnClickListener {
            imageUtils.setTextColor(selectedId, pixelColor)
        }
        binding.closeGetcolor.setOnClickListener {
            colorPickerBottomSheetBehavior.state=BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun checkClickedItem(id:Int, flag: MyFlags?){
        when(id){
            R.id.gallery->{
                dialog.dismiss()
                getCroppedImageLauncher.launch(options {
                    setGuidelines(CropImageView.Guidelines.ON)
                    setAspectRatio(16, 9)
                })
            }
            R.id.color->{
                dialog.dismiss()
                showColorPickerDialog(flag)
            }
            R.id.submit_text->{
                val text=findViewById<EditText>(R.id.text_et).text.toString()
                if (flag==MyFlags.ADD_TEXT){
                    imageUtils.addText(text,++lastId){
                        selectedId=it
                        selectView()
                    }
                    selectedId=lastId
                }else if (flag==MyFlags.EDIT_TEXT){
                    imageUtils.editText(selectedId, text)
                }
                dialog.dismiss()
                val imm=getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            }
            R.id.move_up->{
                if (flag==null)
                    imageUtils.moveText(selectedId, MoveFlag.TOP,
                                    findViewById<Slider>(R.id.move_slider).value.toInt())
                else
                    imageUtils.moveImage(selectedId, MoveFlag.TOP,
                                    findViewById<Slider>(R.id.move_slider).value.toInt())
            }
            R.id.move_down->{
                if (flag==null)
                    imageUtils.moveText(selectedId, MoveFlag.BOTTOM,
                        findViewById<Slider>(R.id.move_slider).value.toInt())
                else
                    imageUtils.moveImage(selectedId, MoveFlag.BOTTOM,
                        findViewById<Slider>(R.id.move_slider).value.toInt())
            }
            R.id.move_left->{
                if (flag==null)
                    imageUtils.moveText(selectedId, MoveFlag.LEFT,
                        findViewById<Slider>(R.id.move_slider).value.toInt())
                else
                    imageUtils.moveImage(selectedId, MoveFlag.LEFT,
                        findViewById<Slider>(R.id.move_slider).value.toInt())
            }
            R.id.move_right->{
                if (flag==null)
                    imageUtils.moveText(selectedId, MoveFlag.RIGHT,
                        findViewById<Slider>(R.id.move_slider).value.toInt())
                else
                    imageUtils.moveImage(selectedId, MoveFlag.RIGHT,
                        findViewById<Slider>(R.id.move_slider).value.toInt())
            }
            R.id.select_stroke_color->{
                showColorPickerDialog(
                    MyFlags.STROKE_COLOR,
                    imageUtils.getStrokeColor(selectedId)
                )
            }
            R.id.align_left->{
                imageUtils.setTextAlign(selectedId, Gravity.LEFT)
            }
            R.id.align_center->{
                imageUtils.setTextAlign(selectedId, Gravity.CENTER_HORIZONTAL)
            }
            R.id.align_right->{
                imageUtils.setTextAlign(selectedId, Gravity.RIGHT)
            }
        }
    }

    private fun selectView()=imageUtils.changeBorderState(selectedId)

    fun showColorPickerDialog(flag: MyFlags?, color:Int=currentColor){
        val dialogBinding:DialogColorpickerBinding=DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.dialog_colorpicker, null, false
        )
        dialogBinding.colorPanelNew.color=color
        dialogBinding.colorPanelOld.color=color
        dialogBinding.colorPickerView.color=color

        dialogBinding.colorPickerView.setOnColorChangedListener {
            dialogBinding.colorPanelNew.color=it
        }

        val dialog=MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.show()

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.okButton.setOnClickListener {
            currentColor=dialogBinding.colorPickerView.color
            dialog.dismiss()
            setColor(currentColor, flag)
        }
    }

    private fun setColor(color: Int, flag: MyFlags?){
        when(flag){
            MyFlags.BACKGROUND->{
                originalImageUri=null
                binding.mainLayout.visibility=GONE
                binding.vgCanvas.visibility= VISIBLE
                binding.saveBtn.visibility= VISIBLE
                binding.imagebackground.setBackgroundColor(color)
                binding.imagebackground.setImageURI(null)
            }
            MyFlags.TEXT_COLOR->imageUtils.setTextColor(selectedId, currentColor)
            MyFlags.GRADIENT1->colors[0]=color
            MyFlags.GRADIENT2->colors[1]=color
            MyFlags.STROKE_COLOR->imageUtils.setStrokeColor(selectedId, color)
            MyFlags.BRUSH_COLOR->{
                binding.drawView.setPaintColor(color)
                binding.drawView.inactiveEraser()
                binding.setDrawingBrushBtn.setBackgroundResource(R.drawable.drawer_active_item_background)
                binding.setDrawingEraserBtn.setBackgroundColor(Color.TRANSPARENT)
            }
            else->{}
        }
    }

    fun startDrawing(){
        drawerBottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED
        binding.brushWidthSlider.value=binding.drawView.getPaintWidth()

        binding.brushWidthSlider.addOnChangeListener { _, value, _ ->
            binding.drawView.setPaintWidth(value)
        }

        binding.setDrawingBrushBtn.setOnClickListener {
            binding.drawView.inactiveEraser()
            binding.setDrawingBrushBtn.setBackgroundResource(R.drawable.drawer_active_item_background)
            binding.setDrawingEraserBtn.setBackgroundColor(Color.TRANSPARENT)
        }
        binding.setDrawingEraserBtn.setOnClickListener {
            binding.drawView.activeEraser()
            binding.setDrawingEraserBtn.setBackgroundResource(R.drawable.drawer_active_item_background)
            binding.setDrawingBrushBtn.setBackgroundColor(Color.TRANSPARENT)
        }
        binding.setDrawingColorBtn.setOnClickListener {
            showColorPickerDialog(
                MyFlags.BRUSH_COLOR,
                binding.drawView.getPaintColor()
            )
        }
        binding.closeDrawing.setOnClickListener {
            drawerBottomSheetBehavior.state=BottomSheetBehavior.STATE_COLLAPSED
        }
    }
}