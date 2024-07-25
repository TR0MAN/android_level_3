package com.example.android_level_3.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.android_level_3.R
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

        // back to previous fragment, when pressed "BACK" in ToolBar
        binding.customToolbarProfile.imgBackToolbarProfile.setOnClickListener {
            findNavController().popBackStack()
        }

        // inserting user data into the appropriate fields
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