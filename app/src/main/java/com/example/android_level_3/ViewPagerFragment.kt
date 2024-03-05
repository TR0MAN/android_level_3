package com.example.android_level_3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android_level_3.adapter.ViewPagerAdapter
import com.example.android_level_3.databinding.FragmentViewPagerBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentViewPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewPagerBinding.inflate(inflater, container, false)

        binding.viewPager.adapter = ViewPagerAdapter(requireActivity(), FRAGMENT_LIST)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = TAB_NAMES[position]
            tab.setIcon(TAB_ICONS[position])
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { tab.icon?.alpha = 250 }
            override fun onTabUnselected(tab: TabLayout.Tab) { tab.icon?.alpha = 70 }
            override fun onTabReselected(tab: TabLayout.Tab) { }
        })

        return binding.root
    }

    companion object {
        val FRAGMENT_LIST = listOf(FragmentSettings(), FragmentContactsList())
        val TAB_NAMES = listOf("Profile", "Contacts")
        val TAB_ICONS = listOf(R.drawable.baseline_profile_24, R.drawable.baseline_contact_24)
    }
}