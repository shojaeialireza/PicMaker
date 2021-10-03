package ir.shojaei.picmaker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import ir.shojaei.picview.PicImageView
import ir.shojaei.picview.PicTextView
import ir.shojaei.picview.PicView
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.gpu.*

class ImageUtils(private val context: Context, private val layout:FrameLayout) {

    //region PicTextView
    fun addText(text:String, id:Int, color:Int=Color.WHITE, size:Float=25f, listener:(id:Int)->Unit){
        if (text.isNotEmpty()){
            val picTextView=PicTextView(context).apply {
                setText(text)
                setTexSize(size)
                setColor(color)
                setId(id)
                setOnClickListener(listener)
            }
            layout.addView(picTextView)
            changeBorderState(id)
        }
    }

    fun setTextColor(id: Int, color: Int){
        getPicTextView(id)?.let {
            it.setColor(color)
        }
    }

    fun getTextSize(id:Int)= getPicTextView(id)?.let { it.getTextSize() }?:0

    fun setTextSize(id: Int, size: Float)= getPicTextView(id)?.let { it.setTexSize(size) }

    fun getText(id: Int)= getPicTextView(id)?.let { it.getText() }?:""

    fun editText(id: Int, text: String)=getPicTextView(id)?.let { it.setText(text) }

    fun setFont(id: Int, font:String)=
        getPicTextView(id)?.let { it.setFont(Typeface.createFromAsset(context.assets, font)) }

    fun setGradient(id: Int, values:List<Int>, colors:List<Int>){
        getPicTextView(id)?.let {
            it.setGradient(values, colors)
        }
    }

    fun getRotation(id: Int):Map<RotationFlag, Int>?{
        return getPicTextView(id)?.let {view->
            val m= mutableMapOf<RotationFlag, Int>()
            m[RotationFlag.ROTATION]=view.getViewRotation().toInt()
            m[RotationFlag.ROTATION_X]=view.getViewRotationX().toInt()
            m[RotationFlag.ROTATION_Y]=view.getViewRotationY().toInt()
            return m
        }
    }

    fun setRotation(id: Int, value:Float, flag: RotationFlag){
        getPicTextView(id)?.let { view->
            when(flag){
                RotationFlag.ROTATION->view.rotation=value
                RotationFlag.ROTATION_X->view.setRotationXY(value, view.getViewRotationY())
                RotationFlag.ROTATION_Y->view.setRotationXY(view.getViewRotationX(), value)
            }
        }
    }

    fun getStrokeSize(id: Int):Int=getPicTextView(id)?.let { it.getStrokeSize() }?:0

    fun getStrokeSize(id: Int, value: Int)=getPicTextView(id)?.let { it.setStroke(value) }

    fun getStrokeColor(id: Int):Int=getPicTextView(id)?.let { it.getStrokeColor() }?:0

    fun setStrokeColor(id: Int, value: Int)=getPicTextView(id)?.let { it.setStrokeColor(value) }

    fun setTextAlign(id: Int, gravity:Int)=getPicTextView(id)?.let { it.setGravityVal(gravity) }

    fun getShadow(id: Int)=getPicTextView(id)?.let { it.getShadow() }?:0f

    fun setShadow(id: Int, value: Float)=getPicTextView(id)?.let { it.setShadow(value) }

    fun copyText(id: Int, newId:Int, listener: (id: Int) -> Unit)=getPicTextView(id)?.let {view->
        val newView = PicTextView(context)
        newView.apply {
            setOnClickListener(listener)
            setId(newId)
            setRotationXY(view.getViewRotationX(), view.getViewRotationY())
            rotation = view.getViewRotation()
            setFont(view.getFont())
            setGravityVal(view.getGravity())
            setText(view.getText())
            setTexSize(view.getTextSize().toFloat())
            setGradient(view.getGradientVals(), view.getGradientColors().toList())
            setOpacity(view.getOpacity())
            setStroke(view.getStrokeSize())
            setStrokeColor(view.getStrokeColor())
            setShadow(view.getShadow())
        }
        layout.addView(newView)
        changeBorderState(newId)
    }

    fun getTextOpacity(id: Int)=getPicTextView(id)?.let { it.getOpacity()*100 }?:0f

    fun setTextOpacity(id: Int, value: Float)=getPicTextView(id)?.let { it.setOpacity(value/100) }

    fun moveText(id: Int, flag: MoveFlag, value: Int){
        getPicTextView(id)?.let {
            when(flag){
                MoveFlag.TOP->it.y-=value
                MoveFlag.BOTTOM->it.y+=value
                MoveFlag.RIGHT->it.x+=value
                MoveFlag.LEFT->it.x-=value
            }
        }
    }

    private fun getPicTextView(id: Int):PicTextView?{
        val view=layout.findViewById<View>(id)
        return if (view is PicTextView)view else null
    }
    //endregion

    fun addImage(id: Int, image:Bitmap, listener: (id: Int) -> Unit){
        val picImageView=PicImageView(context).apply {
            setBitmap(image)
            setId(id)
            setOnClickListener(listener)
        }
        layout.addView(picImageView)
        changeBorderState(id)
    }

    fun getImageOpacity(id: Int)=getPicImageView(id)?.let { it.getOpacity()*100 }?:0f

    fun setImageOpacity(id: Int, value: Float)=getPicImageView(id)?.let { it.setOpacity(value/100) }

    fun moveImage(id: Int, flag: MoveFlag, value: Int){
        getPicImageView(id)?.let {
            when(flag){
                MoveFlag.TOP->it.y-=value
                MoveFlag.BOTTOM->it.y+=value
                MoveFlag.RIGHT->it.x+=value
                MoveFlag.LEFT->it.x-=value
            }
        }
    }

    private fun getPicImageView(id: Int):PicImageView?{
        val view=layout.findViewById<View>(id)
        return if (view is PicImageView)view else null
    }

    fun getFilters():List<RequestOptions>{
        val filterList= mutableListOf<RequestOptions>()
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context, R.color.filter1))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter2))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter3))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter4))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter5))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter6))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter7))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter8))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter9))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter10))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter11))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter12))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter13))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter14))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter15))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter16))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter17))))
        filterList.add(bitmapTransform(ColorFilterTransformation(ContextCompat.getColor(context,R.color.filter18))))
        filterList.add(bitmapTransform(ToonFilterTransformation()))
        filterList.add(bitmapTransform(GrayscaleTransformation()))
        filterList.add(bitmapTransform(BlurTransformation(10)))
        filterList.add(bitmapTransform(ToonFilterTransformation()))
        filterList.add(bitmapTransform(SepiaFilterTransformation()))
        filterList.add(bitmapTransform(SwirlFilterTransformation()))
        filterList.add(bitmapTransform(ContrastFilterTransformation()))
        filterList.add(bitmapTransform(InvertFilterTransformation()))
        filterList.add(bitmapTransform(PixelationFilterTransformation()))
        filterList.add(bitmapTransform(SketchFilterTransformation()))
        filterList.add(bitmapTransform(BrightnessFilterTransformation()))
        filterList.add(bitmapTransform(KuwaharaFilterTransformation()))
        filterList.add(bitmapTransform(VignetteFilterTransformation()))
        return filterList
    }

    fun changeBorderState(id: Int){
        layout.children.forEach {
            if (it is PicView){
                if (it.id==id)
                    it.showBorder()
                else
                    it.hideBorder()
            }
        }
    }
}