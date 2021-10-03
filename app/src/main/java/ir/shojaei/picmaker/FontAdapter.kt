package ir.shojaei.picmaker

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ir.shojaei.picmaker.databinding.FontListItemBinding

class FontAdapter(private val txt:String, private val fontList:List<String>,
private val onItemClick:(id:Int)->Unit):RecyclerView.Adapter<FontAdapter.VH>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):VH{
        context=parent.context
        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.font_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(fontList[position])
    }

    override fun getItemCount()=fontList.size

    inner class VH(private val binding: FontListItemBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(font:String){
            binding.text.apply {
                text=txt
                typeface= Typeface.createFromAsset(context.assets, font)
                setOnClickListener { onItemClick.invoke(adapterPosition) }
            }
        }
    }
}