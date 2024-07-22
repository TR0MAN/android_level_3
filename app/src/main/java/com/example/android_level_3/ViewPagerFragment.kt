package com.example.android_level_3

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_level_3.adapter.ViewPagerAdapter
import com.example.android_level_3.databinding.FragmentViewPagerBinding
import com.example.android_level_3.viewmodel.SharedViewModel
import com.example.android_level_3.viewmodel.SharedViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment() {

    private lateinit var binding: FragmentViewPagerBinding

//    val viewModel by lazy { ViewModelProvider(requireActivity(),
//        SharedViewModelFactory(requireContext())).get(SharedViewModel::class.java) }

    private val viewModel: SharedViewModel by activityViewModels()

//    private val viewModel: SharedViewModel by viewModels<SharedViewModel> { SharedViewModelFactory(requireActivity(), this.javaClass.toString()) }
//    private val viewModel: SharedViewModel by activityViewModels { SharedViewModelFactory(requireActivity(), this.javaClass.toString()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewPagerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Log.d("TAG", "ViewPagerFragment -> onViewCreated")
        Log.d("TAG", "ViewPagerFragment -> USE[$viewModel]")
        Log.d("TAG", "ViewPagerFragment -> DATA STORAGE -> [${viewModel.dataStorage}]")
        Log.d("TAG", "ViewPagerFragment [END] -> -----------------------------------")

        initViewPager()
        initTabLayout()
        setObservers()
    }

    private fun initTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { tab.icon?.alpha = 250 }
            override fun onTabUnselected(tab: TabLayout.Tab) { tab.icon?.alpha = 70 }
            override fun onTabReselected(tab: TabLayout.Tab) { }
        })
    }

    private fun initViewPager() {
        binding.viewPager.adapter = ViewPagerAdapter(requireActivity(), FRAGMENT_LIST)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = TAB_NAMES[position]
            tab.setIcon(TAB_ICONS[position])
        }.attach()
    }

    private fun setObservers() {
        viewModel.tabLayoutVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.tabLayout.visibility = if (visibility) View.VISIBLE else View.GONE
        }
    }

    companion object {
        val FRAGMENT_LIST = listOf(FragmentSettings(), FragmentContactsList())
        val TAB_NAMES = listOf("Profile", "Contacts")
        val TAB_ICONS = listOf(R.drawable.baseline_profile_24, R.drawable.baseline_contact_24)
    }
}