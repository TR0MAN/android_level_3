package com.example.android_level_3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.android_level_3.databinding.FragmentSettingsBinding

class FragmentSettings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.btnSettingsViewContacts.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSettings_to_fragmentContactsList)
        }

        return binding.root
    }

}