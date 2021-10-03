package ir.shojaei.picmaker

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ir.shojaei.picmaker.databinding.EffectListItemLayoutBinding

class FilterAdapter(
    private val image: Uri, private val filterList: List<RequestOptions>,
    private val onItemClick: (filter: RequestOptions?) -> Unit
) : RecyclerView.Adapter<FilterAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.effect_list_item_layout,
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(
            if (position == 0) null else filterList[position - 1]
        )
    }

    override fun getItemCount() = filterList.size + 1

    inner class VH(private val binding: EffectListItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(filter: RequestOptions?) {
            if (filter == null) {
                Glide.with(binding.root).load(image).into(binding.imgFilter)
            } else {
                Glide.with(binding.root).load(image).apply(filter).into(binding.imgFilter)
            }
            binding.imgFilter.setOnClickListener { onItemClick.invoke(filter) }
        }
    }
}