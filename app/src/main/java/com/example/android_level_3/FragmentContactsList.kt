package com.example.android_level_3

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_level_3.adapter.ContactAdapter
import com.example.android_level_3.adapter.ElementClickListener
import com.example.android_level_3.constants.Const
import com.example.android_level_3.constants.RequestConst
import com.example.android_level_3.databinding.FragmentContactsListBinding
import com.example.android_level_3.retrofit.model.Contact
import com.example.android_level_3.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class FragmentContactsList : Fragment() {

    private lateinit var binding: FragmentContactsListBinding
    private lateinit var recyclerViewAdapter: ContactAdapter
    private lateinit var actionListener: ElementClickListener

    private val viewModel: SharedViewModel by activityViewModels()

    // Snackbar for restore deleted user
    private var informationSnackbar: Snackbar? = null
    // Snackbar for showing message about connection problem
    private var connectionErrorSnackbar: Snackbar? = null
    private var contactIdForDelete: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        initElementClickListener()
        setFragmentButtonsListeners()

        if (viewModel.tabLayoutVisibility.value == true) {
            requestGetUserContacts()
        }
    }

    // request for getting list of contacts
    private fun requestGetUserContacts() {
        viewModel.getUserContacts(userId = viewModel.userId.value!!, token = viewModel.token.value!!,
            progressBar = viewModel.isVisibleProgressBarInFragmentContactsList)
    }

    // request for deleting from user contacts list
    private fun requestDeleteFromContacts() {
        viewModel.deleteFromContacts(
            userId = viewModel.userId.value!!, token = viewModel.token.value!!,
            idForDelete = contactIdForDelete!!,
            progressBar = viewModel.isVisibleProgressBarInFragmentContactsList)
    }

    // request for group deleting from user contacts list (many times deleting one contact)
    private fun deleteMultipleContact(contactsForGroupDelete: MutableSet<Int>?) {
        viewModel.deleteFromContacts( userId = viewModel.userId.value!!,
            token = viewModel.token.value!!, idForDelete = contactsForGroupDelete!!.first(),
            progressBar = viewModel.isVisibleProgressBarInFragmentContactsList)
    }

    // request for restore deleting contact (just adding deleted contact)
    private fun requestRestoreInContactList() {
        viewModel.addToContactList(userId = viewModel.userId.value!!, token = viewModel.token.value!!,
            contactId = contactIdForDelete!!,
            progressBar = viewModel.isVisibleProgressBarInFragmentContactsList)
    }

    // initializing observers
    private fun setObservers() {

        // observer for update List Adapter - (OK)
        viewModel.listWithAllContacts.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                binding.recyclerViewContacts.visibility = View.VISIBLE
                binding.noContactsContainer.visibility = View.GONE

                // updating list of user contacts ID
                viewModel.userContactsIdList.clear()
                viewModel.userContactsIdList.addAll(list.map { it.id })

                // restore state after rotate screen (list for group deleting and checked contacts)
                if (viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                    createAdapter(multiSelectState = true,
                        selectedContacts = viewModel.listSelectedContactsForGroupDelete.value?.toList(),
                        contactsList = viewModel.listWithAllContacts.value)
                } else {
                    // restoring state of search panel (if panel be visible before rotating)
                    if (viewModel.isActiveSearchUserContacts.value == true) {
                        actionAfterFiltered()
                    } else {
                        // action after rotate screen
                        createAdapter(multiSelectState = false, selectedContacts = null)
                        recyclerViewAdapter.submitList(list)
                    }
                }
            } else {
                binding.recyclerViewContacts.visibility = View.GONE
                binding.noContactsContainer.visibility = View.VISIBLE
            }
        }

        // contact list observer for deletion
        viewModel.listSelectedContactsForGroupDelete.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                createAdapter(multiSelectState = false, selectedContacts = null)
                viewModel.tabLayoutVisibility.value = true
            }
        }

        // observing result of a contact list request
        viewModel.getUserContactsResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {                                                           // показываем Snackbar с ошибкой + можно перезапустить запрос
                connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_GET_CONTACTS)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listWithAllContacts.value = serverResponse.body()?.data?.contacts
            } else {
                connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_GET_CONTACTS)
                connectionErrorSnackbar?.show()
            }
        }

        // observing the result of a deletion request
        viewModel.deleteFromContactsResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {

                // case with group deletion
                if(viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                    connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_GROUP_DELETE)
                    connectionErrorSnackbar?.show()
                } else {
                    connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_DELETE_CONTACT)
                    connectionErrorSnackbar?.show()
                }
            } else if (serverResponse.isSuccessful) {
                // case with group deletion
                if (viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                    val currentIdForDelete = viewModel.listSelectedContactsForGroupDelete.value?.first()
                    viewModel.listSelectedContactsForGroupDelete.value?.remove(currentIdForDelete)
                    viewModel.userContactsIdList.remove(currentIdForDelete)

                    if (viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                        deleteMultipleContact(viewModel.listSelectedContactsForGroupDelete.value)
                    }
                }

                viewModel.listWithAllContacts.value = serverResponse.body()?.data?.contacts

            } else {
                // trying to delete a contact that is not in the contact list
                if (serverResponse.body()?.message.equals("Contact not found")) {
                    requestGetUserContacts()
                }
                // case with group deletion
                if (viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                    connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_GROUP_DELETE)
                    connectionErrorSnackbar?.show()
                } else {
                    connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_DELETE_CONTACT)
                    connectionErrorSnackbar?.show()
                }
            }
        }

        // observing the result of a addition request
        viewModel.addToContactListResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_RESTORE_CONTACT)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listWithAllContacts.value = serverResponse.body()?.data?.contacts
            } else {
                connectionErrorSnackbar = createConnectionSnackBar(RequestConst.REQUEST_RESTORE_CONTACT)
                connectionErrorSnackbar?.show()
            }
        }

        viewModel.isVisibleProgressBarInFragmentContactsList.observe(viewLifecycleOwner) { visibility ->
            if (visibility) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        viewModel.isActiveSearchUserContacts.observe(viewLifecycleOwner) { visible ->
            with(binding) {
                if (visible) {
                    toolbarContactList.containerTextContactList.visibility = View.GONE
                    toolbarContactList.containerSearchContactList.visibility = View.VISIBLE
                } else {
                    toolbarContactList.containerTextContactList.visibility = View.VISIBLE
                    toolbarContactList.containerSearchContactList.visibility = View.GONE
                    toolbarContactList.edSearchContactList.setText("")
                }
            }
        }
    }

    // initializing listeners for clicks on list items
    private fun initElementClickListener() {

        actionListener = object : ElementClickListener {

            // listener for contact deleting
            override fun onElementClickAction(contact: Contact) {
                contactIdForDelete = contact.id
                requestDeleteFromContacts()
                createInformationSnackbar()
                informationSnackbar?.show()
            }

            // listener for go to contact profile
            override fun onElementProfileClick(contact: Contact) {
                val destinationPointWithData = ViewPagerFragmentDirections
                    .actionViewPagerFragmentToFragmentContactProfile(contact)
                findNavController().navigate(destinationPointWithData)
                informationSnackbar?.dismiss()
            }

            // listener to switch to group delete mode
            override fun onElementLongClick(contactId: Int) {
                viewModel.listSelectedContactsForGroupDelete.value =
                    viewModel.listSelectedContactsForGroupDelete.value?.apply { add(contactId) }
                createAdapter(multiSelectState = true,
                    selectedContacts = viewModel.listSelectedContactsForGroupDelete.value?.toList())
                viewModel.tabLayoutVisibility.value = false
            }

            // listener for clicks on an element in group deletion mode
            override fun onElementChecked(checkBoxState: Boolean, contactId: Int) {
                if (checkBoxState) {
                    viewModel.listSelectedContactsForGroupDelete.value =
                        viewModel.listSelectedContactsForGroupDelete.value?.apply { add(contactId) }
                } else {
                    val status = viewModel.listSelectedContactsForGroupDelete.value?.contains(contactId)!!
                    if (status) {
                        viewModel.listSelectedContactsForGroupDelete.value =
                            viewModel.listSelectedContactsForGroupDelete.value?.apply { remove(contactId) }
                    }
                }
            }
        }
    }

    private fun createInformationSnackbar() {
        informationSnackbar = Snackbar.make(binding.root, getString(R.string.snackbar_button_message), Const.SNACKBAR_DURATION)
            .setActionTextColor(requireContext().getColor(R.color.orange_color))
            .setAction(getString(R.string.snackbar_button_text)) {
                requestRestoreInContactList()
            }
    }

    // initialization button listeners
    private fun setFragmentButtonsListeners() {

        binding.tvAddNewContact.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_fragmentAddContact)
            informationSnackbar?.dismiss()
        }

        // group deleting button
        binding.imgDeleteManyContacts.setOnClickListener {
            deleteMultipleContact(viewModel.listSelectedContactsForGroupDelete.value)
            viewModel.tabLayoutVisibility.value = true
        }

        binding.toolbarContactList.edSearchContactList.doOnTextChanged { text, _, _, _ ->
            if(text?.isNotEmpty() == true ) {
                viewModel.filteredContactsList.value?.clear()

                viewModel.listWithAllContacts.value?.forEach { contact ->
                    if (contact.name?.contains(text, true) == true) {
                        viewModel.filteredContactsList.value =
                            viewModel.filteredContactsList.value?.apply { add(contact) }
                    }
                }
                actionAfterFiltered()
            } else {
                if (viewModel.listWithAllContacts.value?.isNotEmpty() == true) {
                    viewModel.listWithAllContacts.value = viewModel.listWithAllContacts.value
                }
            }
        }

        binding.toolbarContactList.imgCloseContactList.setOnClickListener {                  // слушатель на кнопку "закрыть панель поиска"
            viewModel.isActiveSearchUserContacts.value = false
        }

        // toolbar button "search"
        binding.toolbarContactList.imgSearchContactList.setOnClickListener {                 // слушатель на кнопку "открыть панель поиска"
            viewModel.isActiveSearchUserContacts.value = true
        }

        // Возврата на предыдущий фрагмент нет, фрагменты в TabLayout (удалить?)
//        binding.toolbarContactList.imgBackContactList.setOnClickListener {
//            findNavController().popBackStack()
//        }
    }

    // displaying the search result after filtering
    private fun actionAfterFiltered() {
        if (viewModel.filteredContactsList.value?.isNotEmpty() == true) {
            binding.recyclerViewContacts.visibility = View.VISIBLE
            binding.noContactsContainer.visibility = View.GONE

            createAdapter(multiSelectState = false, selectedContacts = null,
                contactsList = viewModel.filteredContactsList.value)
        } else {
            binding.recyclerViewContacts.visibility = View.GONE
            binding.noContactsContainer.visibility = View.VISIBLE
        }
    }


    // creating adapter for RecyclerView, with different states
    private fun createAdapter(multiSelectState: Boolean, selectedContacts: List<Int>?,
                              contactsList: List<Contact>? = null) {
        binding.imgDeleteManyContacts.visibility = if (multiSelectState) View.VISIBLE else View.GONE
        recyclerViewAdapter = ContactAdapter(actionListener, multiSelectState, null, selectedContacts)
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContacts.adapter = recyclerViewAdapter
        if (contactsList == null) {
            recyclerViewAdapter.submitList(viewModel.listWithAllContacts.value)
        }
        else {
            recyclerViewAdapter.submitList(contactsList)
        }
    }

    private fun createConnectionSnackBar(requestType: String): Snackbar {
        return Snackbar.make(binding.root,
            getString(R.string.connection_error_snackbar_message), Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(requireActivity().getColor(R.color.orange_color))
            .setAction(getString(R.string.connection_error_snackbar_action_button_text)) {
                when(requestType){
                    RequestConst.REQUEST_GET_CONTACTS -> { requestGetUserContacts() }
                    RequestConst.REQUEST_DELETE_CONTACT -> { requestDeleteFromContacts() }
                    RequestConst.REQUEST_RESTORE_CONTACT -> { requestRestoreInContactList() }
                    RequestConst.REQUEST_GROUP_DELETE -> {
                        deleteMultipleContact(viewModel.listSelectedContactsForGroupDelete.value)
                    }
                }
            }
    }

    override fun onStop() {
        super.onStop()
        connectionErrorSnackbar?.dismiss()
    }

}
