package com.example.android_level_3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.android_level_3.constants.Const
import com.example.android_level_3.databinding.FragmentProfileInviteBinding

class FragmentProfileInvite : Fragment() {

    private lateinit var binding: FragmentProfileInviteBinding
    private val args: FragmentProfileInviteArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileInviteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setContactDataToUI()
        setButtonListeners()
    }

    private fun setButtonListeners() {

        // отслеживание кнопки добавления контакта
        binding.btnDetailProfileAddToContacts.setOnClickListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.RESULT_KEY, args.contactInformation.id)
            findNavController().popBackStack()
        }

        // back to previous fragment
        binding.customToolbarProfile.imgBackToolbarProfile.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // вставка полученных данных о пользователе в соответствующие поля
    private fun setContactDataToUI() {
        with(binding) {
            tvProfileName.text = args.contactInformation.name
            tvProfileProfession.text = args.contactInformation.career
            tvProfileAddress.text = args.contactInformation.address
            Glide.with(binding.imgProfileMainPhoto.context)
                .load(args.contactInformation.image)
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .into(binding.imgProfileMainPhoto)
        }
    }


}