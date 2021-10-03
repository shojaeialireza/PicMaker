package ir.shojaei.picmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ir.shojaei.picmaker.databinding.ImagefragmentLayoutBinding

class ImageFragment:Fragment() {

    private lateinit var binding: ImagefragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=DataBindingUtil.inflate(inflater,R.layout.imagefragment_layout,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setimage.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_getimage, MyFlags.BACKGROUND)
        }
        binding.seteffect.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_effect, null)
        }
        binding.addImg.setOnClickListener {
            (activity as MainActivity).addImage()
        }
        binding.moveImg.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_move, MyFlags.IMAGE)
        }
        binding.setopacityimage.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_opacity, MyFlags.IMAGE)
        }
        binding.brushImg.setOnClickListener {
            (activity as MainActivity).startDrawing()
        }
    }
}