package com.example.android_level_3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.android_level_3.databinding.FragmentContactProfileBinding

class FragmentContactProfile : Fragment() {

    private lateinit var binding: FragmentContactProfileBinding
    private val args: FragmentContactProfileArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // возврат назад, при нажатии на "стрелочку" возврата в ToolBar
        binding.customToolbarProfile.imgBackToolbarProfile.setOnClickListener {
            findNavController().popBackStack()
        }

        // вставка полученных данных о пользователе в соответствующие поля
        with(binding) {
            tvProfileName.text = args.currentUserProfile.name
            tvProfileProfession.text = args.currentUserProfile.career
            tvProfileAddress.text = args.currentUserProfile.address
            Glide.with(binding.imgProfileMainPhoto.context)
                .load(args.currentUserProfile.image)
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .into(binding.imgProfileMainPhoto)
        }
    }
}