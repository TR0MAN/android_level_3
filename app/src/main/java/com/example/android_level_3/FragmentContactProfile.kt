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
import com.example.android_level_3.model.Contact

class FragmentContactProfile : Fragment() {

    private lateinit var binding: FragmentContactProfileBinding
    private val args: FragmentContactProfileArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactProfileBinding.inflate(inflater, container, false)

        // возврат назад, при нажатии на "стрелочку" возврата в ToolBar
        binding.customToolbarProfile.imgBackToolbarProfile.setOnClickListener {
            findNavController().popBackStack()
        }

        // вставка полученных данных о пользователе в соответствующие поля
        with(binding) {
            tvProfileName.text = args.currentUserProfile.contactName
            tvProfileProfession.text = args.currentUserProfile.contactCareer
            tvProfileAddress.text = args.currentUserProfile.contactAddress
            Glide.with(binding.imgProfileMainPhoto.context)
                .load(args.currentUserProfile.contactImage)
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .into(binding.imgProfileMainPhoto)
        }

        return binding.root
    }
}