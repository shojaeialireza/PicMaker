package ir.shojaei.picmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ir.shojaei.picmaker.databinding.TextfragmentLayoutBinding

class TextFragment:Fragment() {

    private lateinit var binding: TextfragmentLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=DataBindingUtil.inflate(inflater,R.layout.textfragment_layout,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.newText.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_gettext, MyFlags.ADD_TEXT)
        }
        binding.editText.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_gettext, MyFlags.EDIT_TEXT)
        }
        binding.textColor.setOnClickListener {
            (activity as MainActivity).showColorPickerDialog(MyFlags.TEXT_COLOR)
        }
        binding.textSize.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_textsize, null)
        }
        binding.fontPer.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_font, MyFlags.PER_FONT)
        }
        binding.fontEng.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_font, MyFlags.ENG_FONT)
        }
        binding.textGradient.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_gradient, null)
        }
        binding.textRotate.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_textrotate, null)
        }
        binding.textMove.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_move, null)
        }
        binding.colorPicker.setOnClickListener {
            (activity as MainActivity).startColorPicker()
        }
        binding.textStroke.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_textstroke, MyFlags.STROKE_COLOR)
        }
        binding.textAlign.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_textalign, null)
        }
        binding.textShadow.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_textshadow, null)
        }
        binding.textOpacity.setOnClickListener {
            (activity as MainActivity).showDialogPlus(R.layout.dialog_opacity, null)
        }
        binding.textCopy.setOnClickListener {
            (activity as MainActivity).copyText()
        }
    }
}