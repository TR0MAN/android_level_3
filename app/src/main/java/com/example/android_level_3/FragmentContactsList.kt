package com.example.android_level_3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_level_3.adapter.ContactAdapter
import com.example.android_level_3.adapter.ElementClickListener
import com.example.android_level_3.databinding.FragmentContactsListBinding
import com.example.android_level_3.model.Contact
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar

class FragmentContactsList : Fragment() {

    private lateinit var binding: FragmentContactsListBinding
    private lateinit var recyclerViewAdapter: ContactAdapter
    private lateinit var actionListener: ElementClickListener

    private val viewModel: MainViewModel by activityViewModels()
    private val selectedContactList = MutableLiveData<MutableSet<Int>>(mutableSetOf())
    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)

        setObservers()
        initElementClickListener()
        setFragmentButtonsListeners()

        return binding.root
    }

    // restore selected elements to list, after rotate screen
    override fun onStart() {
        super.onStart()
        if (viewModel.checkedContactList.isNotEmpty()) {
            selectedContactList.value = viewModel.checkedContactList.toMutableSet()
            createAdapter(multiSelectState = true)
        }
    }

    // save selected elements to list, if rotate screen
    override fun onPause() {
        super.onPause()
        if (selectedContactList.value?.isEmpty() == false) {
            viewModel.checkedContactList.addAll(selectedContactList.value!!)
        }
    }

    // initializing observers
    private fun setObservers() {
        // contact list observer for deletion
        selectedContactList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                createAdapter(multiSelectState = false)
                viewModel.tabLayoutVisibility.value = true
            }
        }

        // observer for update List Adapter
        viewModel.observableContactList.observe(viewLifecycleOwner) { contactList ->
            recyclerViewAdapter.submitList(contactList)
        }

        // get new contact from dialog fragment
        val resultOfAddingNewContact =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Contact>(Const.RESULT_KEY)
        resultOfAddingNewContact?.observe(viewLifecycleOwner) { newContact ->
            if (newContact != null) {
                viewModel.addContact(newContact)
                resultOfAddingNewContact.value = null
            }
        }
    }

    // initializing listeners for clicks on list items
    private fun initElementClickListener() {

        actionListener = object : ElementClickListener {

            override fun onElementDeleteClick(contact: Contact) {
                viewModel.deleteContact(contact)
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
                viewModel.tabLayoutVisibility.value = false
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

    private fun createSnackbar() {
        snackbar = Snackbar.make(binding.root, getString(R.string.snackbar_button_message), 5000)
            .setActionTextColor(requireContext().getColor(R.color.orange_color))
            .setAction(getString(R.string.snackbar_button_text)) {
                viewModel.restoreContact()
            }
    }

    // initialization button listeners
    private fun setFragmentButtonsListeners() {

        binding.tvAddNewContact.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_customDialog)
            snackbar?.dismiss()
        }

        // group deleting button
        binding.imgDeleteManyContacts.setOnClickListener {
            viewModel.deleteMultipleContact(selectedContactList.value?.toList())
            selectedContactList.value = selectedContactList.value?.apply { clear() }
            viewModel.checkedContactList.clear()
            createAdapter(multiSelectState = false)
            viewModel.tabLayoutVisibility.value = true
        }

        // toolbar button "back"
        binding.toolbarContactList.imgBackToolbarContactList.setOnClickListener {
//            findNavController().popBackStack()
        }

        // toolbar button "search"
        binding.toolbarContactList.imgSearchToolbarContactList.setOnClickListener {
            Toast.makeText( requireContext(),
                getString(R.string.toolbar_contact_list_search_button_toast_message),
                Toast.LENGTH_SHORT).show()
        }
    }

    // creating adapter for RecyclerView, with different states
    private fun createAdapter(multiSelectState: Boolean) {
        binding.imgDeleteManyContacts.visibility = if (multiSelectState) View.VISIBLE else View.GONE
        recyclerViewAdapter = ContactAdapter(actionListener, multiSelectState)
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContacts.adapter = recyclerViewAdapter
        recyclerViewAdapter.submitList(viewModel.getContactList())
    }

}
