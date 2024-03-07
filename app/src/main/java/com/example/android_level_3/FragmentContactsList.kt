package com.example.android_level_3

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_level_3.adapter.ContactAdapter
import com.example.android_level_3.adapter.ElementClickListener
import com.example.android_level_3.databinding.FragmentContactsListBinding
import com.example.android_level_3.model.Contact
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar

// TODO
//  1. Разобраться с внешним видом элемента в RecyclerView в режиме группового удаления
//  2. Отработать показ мультивыбора (если он был) при повороте экрана
//  3. Попробовать убирать TabLayout в режиме мультивыбора и возвращать его в режиме просмотра контактов
//  4. Почистить все закомментированные строки и удалить не нужное.

class FragmentContactsList : Fragment() {

    private lateinit var binding: FragmentContactsListBinding
    private lateinit var recyclerViewAdapter: ContactAdapter
//    private val recyclerViewAdapter by lazy { ContactAdapter(actionListener, true) }

    private val viewModel: MainViewModel by viewModels()

    private val selectedContactList = MutableLiveData<MutableSet<Int>>(mutableSetOf())

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

        createAdapter(multiSelectState = false)

        return binding.root
    }

    // инициализация наблюдателей за состоянием таймера обратного отсчета
    private fun setObservers() {

        // наблюдатель за списком контактов на удаление
        selectedContactList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                createAdapter(multiSelectState = false)
                Log.d("TAG", "LIST EMPTY ${selectedContactList.value}")             // TODO - DELETE
            }
            Log.d("TAG", "selectedContactList = ${selectedContactList.value}")      // TODO - DELETE
        }

        // TODO - DELETE
//        listObserver.observe(viewLifecycleOwner) {
//            if (it.isEmpty()) {
////                createCommonList()
//                Log.d("TAG", "LIST EMPTY $listForDelete")
//            }
//        }


        // обновление списка в List Adapter
        viewModel.observableContactList.observe(viewLifecycleOwner) { contactList ->
            recyclerViewAdapter.submitList(contactList)
        }

        // получение нового контакта из далогового фрагмента
        val resultOfAddingNewContact =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Contact>(Const.RESULT_KEY)
        resultOfAddingNewContact?.observe(viewLifecycleOwner) { newContact ->
            if (newContact != null) {
                viewModel.addContact(newContact)
                resultOfAddingNewContact.value = null
            }
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

            override fun onElementLongClick(contactId: Int) {
                viewModel.changeSelectableState(contactId)
                createAdapter(multiSelectState = true)
                selectedContactList.value = selectedContactList.value?.apply {
                    add(contactId)
                }
            }

            override fun onElementChecked(checkBoxState: Boolean, contactId: Int) {
                if (checkBoxState) {
                    selectedContactList.value = selectedContactList.value?.apply { add(contactId) }
                    viewModel.changeSelectableState(contactId)
                } else {
                    val status = selectedContactList.value?.contains(contactId)!!
                    if (status) {
                        selectedContactList.value =
                            selectedContactList.value?.apply { remove(contactId) }
                        viewModel.changeSelectableState(contactId)
                    }
                }
            }
        }
    }

    fun createSnackbar() {
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

        // действия по клику на кнопку удаления ГРУППЫ
        binding.imgDeleteManyContacts.setOnClickListener {
            viewModel.deleteMultipleContact(selectedContactList.value?.toList())
            selectedContactList.value?.toMutableList()?.clear()
            createAdapter(multiSelectState = false)

            // ПРОВЕРИТЬ РАБОТУ - РАБОТАЕТ                  // TODO - DELETE
//            val newSet = mutableSetOf<Int>()
//            selectedContactList.value = newSet
        }

        // реакция на "стрелочку" назад в ToolBar
        binding.toolbarContactList.imgBackToolbarContactList.setOnClickListener {
//            findNavController().popBackStack()
        }

        binding.toolbarContactList.imgSearchToolbarContactList.setOnClickListener {
            Toast.makeText( requireContext(),
                getString(R.string.toolbar_contact_list_search_button_toast_message),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAdapter(multiSelectState: Boolean) {
        binding.imgDeleteManyContacts.visibility = if (multiSelectState) View.VISIBLE else View.GONE
        recyclerViewAdapter = ContactAdapter(actionListener, multiSelectState)
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContacts.adapter = recyclerViewAdapter
        recyclerViewAdapter.submitList(viewModel.getContactList())
    }

    // TODO - DELETE
//    private fun createAdapter(multiSelectState: Boolean) {
//        binding.imgDeleteManyContacts.visibility = if (multiSelectState) View.VISIBLE else View.GONE
//        val newAdapter = ContactAdapter(actionListener, multiSelectState)
//        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerViewContacts.adapter = newAdapter
//    }

}
