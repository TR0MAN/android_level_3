package com.example.android_level_3

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.android_level_3.adapter.ContactAdapter
import com.example.android_level_3.adapter.ElementClickListener
import com.example.android_level_3.databinding.FragmentContactsListBinding
import com.example.android_level_3.model.Contact
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import java.text.FieldPosition


class FragmentContactsList : Fragment() {

    private lateinit var binding: FragmentContactsListBinding

    private val recyclerViewAdapter by lazy { ContactAdapter(actionListener) }
    private val viewModel: MainViewModel by viewModels()

    private lateinit var actionListener: ElementClickListener
    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)

        setObservers()
        initElementClickListener()
        setFragmentButtonsListeners()

        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = recyclerViewAdapter

        return binding.root
    }

    // TODO - DELETE
    // получение обьекта Contact с экрана добавления нового пользователя и добавление его в список
//    private fun getResultFromCustomDialog() {
//        parentFragment?.parentFragmentManager?.setFragmentResultListener(Const.REQUEST_KEY, viewLifecycleOwner){ key, value ->
//            Log.d("TAG", "INSIDE  parentFragmentManager")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                value.getSerializable(Const.RESULT_KEY, Contact::class.java)
//                    ?.let {
//                        Log.d("TAG", "[FRAGMENT] dialog result(it) - $it")
//                        viewModel.addContact(it)
//                    }
//            } else {
//                val test = value.getSerializable(Const.RESULT_KEY) as Contact
//                Log.d("TAG", "[FRAGMENT] dialog result(test) - $test")
//                viewModel.addContact(value.getSerializable(Const.RESULT_KEY) as Contact)
//            }
//        }
//    }

    // инициализация наблюдателей за состоянием таймера обратного отсчета
    private fun setObservers() {

        // обновление списка в List Adapter
        viewModel.observableContactList.observe(viewLifecycleOwner) { contactList ->
            recyclerViewAdapter.submitList(contactList)
        }

        // получение контакта из далогового фрамента
        val result = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Contact>(Const.RESULT_KEY)
        result?.observe(viewLifecycleOwner) { newContact ->
            viewModel.addContact(newContact)
        }
    }

    // инициализация слушателя нажатий по элементам списка
    private fun initElementClickListener() {

        actionListener = object : ElementClickListener {

            override fun onElementDeleteClick(contact: Contact) {
                viewModel.deleteContact(contact)                                                    // TODO - DELETE CONTACT
                createSnackbar()
                snackbar?.show()
            }

            override fun onElementProfileClick(contact: Contact) {
                if (viewModel.observableContactList.value?.contains(contact) == false) return

                val destinationPointWithData = ViewPagerFragmentDirections
                    .actionViewPagerFragmentToFragmentContactProfile(contact)
                findNavController().navigate(destinationPointWithData)
                snackbar?.dismiss()
            }
        }
    }

    fun createSnackbar(){
        snackbar = Snackbar.make(binding.root, getString(R.string.snackbar_button_message), 5000)
            .setActionTextColor(requireContext().getColor(R.color.orange_color))
            .setAction(getString(R.string.snackbar_button_text)) {
                viewModel.restoreContact()
            }
    }

    // инициализация слушателей кнопок фрагмента
    private fun setFragmentButtonsListeners() {

        binding.tvAddNewContact.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_customDialog)
            snackbar?.dismiss()
        }

        // реакция на "стрелочку" назад в ToolBar
        binding.toolbarContactList.imgBackToolbarContactList.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarContactList.imgSearchToolbarContactList.setOnClickListener {
            Toast.makeText(requireContext(),
                getString(R.string.toolbar_contact_list_search_button_toast_message), Toast.LENGTH_SHORT).show()
        }
    }



}
