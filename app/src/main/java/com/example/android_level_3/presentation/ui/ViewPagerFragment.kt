package com.example.android_level_3.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.android_level_3.FragmentProfileSettings
import com.example.android_level_3.R
import com.example.android_level_3.presentation.ui.adapter.ViewPagerAdapter
import com.example.android_level_3.databinding.FragmentViewPagerBinding
import com.example.android_level_3.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentViewPagerBinding

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewPager()
        initTabLayout()
        setObservers()
    }

    private fun initTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.icon?.alpha = SELECTED_TAB_TRANSPARENCY_LEVEL
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.icon?.alpha = UNSELECTED_TAB_TRANSPARENCY_LEVEL
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun initViewPager() {
        val tabs = Tab.entries
        binding.viewPager.adapter = ViewPagerAdapter(requireActivity(), tabs.map { it.fragment })
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            with(tabs[position]) {
                tab.text = name
                tab.setIcon(tabIcon)
            }
        }.attach()
    }

    //todo Get rid of this observer!
    private fun setObservers() {
        viewModel.tabLayoutVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.tabLayout.visibility = if (visibility) View.VISIBLE else View.GONE
        }
    }
    companion object {
        const val SELECTED_TAB_TRANSPARENCY_LEVEL = 250
        const val UNSELECTED_TAB_TRANSPARENCY_LEVEL = 70
    }
}

private enum class Tab(@DrawableRes val tabIcon: Int, val fragment: Fragment) {
    PROFILE(R.drawable.baseline_profile_24, FragmentProfileSettings()),
    CONTACTS(R.drawable.baseline_contact_24, FragmentContactsList()),
}