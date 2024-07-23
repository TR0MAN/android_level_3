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
import com.example.android_level_3.databinding.FragmentAddContactBinding
import com.example.android_level_3.retrofit.model.Contact
import com.example.android_level_3.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class FragmentAddContact : Fragment(), ElementClickListener {

    private lateinit var binding: FragmentAddContactBinding
    private lateinit var recyclerViewAdapter: ContactAdapter
    private val viewModel: SharedViewModel by activityViewModels()

    private var connectionErrorSnackbar: Snackbar? = null
    private var contactId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
        requestGetAllUsers()
    }

    private fun setObservers() {

        viewModel.listOfAllUsers.observe(viewLifecycleOwner) {
            if (viewModel.isActiveSearchAddContact.value == true) {
                createAdapter( multiSelectState = null,
                    usersInContactList = viewModel.userContactsIdList.toList(),
                    filteredContactsList = viewModel.filteredUserList.value?.toList())
            } else {
                createAdapter( multiSelectState = null,
                    usersInContactList = viewModel.userContactsIdList.toList() )
            }
        }

        viewModel.listWithAllContacts.observe(viewLifecycleOwner) { list ->

            // updating list of user contacts ID
            viewModel.userContactsIdList.addAll(list.map { it.id })

            if(viewModel.isActiveSearchAddContact.value == false) {
                createAdapter(multiSelectState = null, usersInContactList = viewModel.userContactsIdList.toList())
            }
        }

        viewModel.addToContactListResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackbar = createConnectionErrorSnackBar(RequestConst.REQUEST_ADD_CONTACT)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listWithAllContacts.value = serverResponse.body()?.data?.contacts         // TODO - ВСТАВЛЯЮ ДАННЫЕ
            } else {
                connectionErrorSnackbar = createConnectionErrorSnackBar(RequestConst.REQUEST_ADD_CONTACT)
                connectionErrorSnackbar?.show()
            }
        }

        viewModel.getAllUsersResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackbar = createConnectionErrorSnackBar(RequestConst.REQUEST_GET_ALL_USERS)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listOfAllUsers.value = serverResponse.body()?.data?.users                 // TODO - ВСТАВЛЯЮ ДАННЫЕ
            } else {
                connectionErrorSnackbar = createConnectionErrorSnackBar(RequestConst.REQUEST_GET_ALL_USERS)
                connectionErrorSnackbar?.show()
            }
        }

        viewModel.isVisibleProgressBarInFragmentAddContact.observe(viewLifecycleOwner) { visibility ->
            if (visibility) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        // show/hide search panel (when rotate screen)
        viewModel.isActiveSearchAddContact.observe(viewLifecycleOwner) { visible ->
            with(binding) {
                if (visible) {
                    toolbarAddContact.containerTextAddContact.visibility = View.GONE
                    toolbarAddContact.containerSearchAddContact.visibility = View.VISIBLE
                } else {
                    toolbarAddContact.containerTextAddContact.visibility = View.VISIBLE
                    toolbarAddContact.containerSearchAddContact.visibility = View.GONE
                    toolbarAddContact.edSearchAddContact.setText("")
                }
            }
        }

        // listener for getting contact id for adding new contact to contact list
        val resultOfAddingContact =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>(Const.RESULT_KEY)

        // observer for adding new contact to contact list
        resultOfAddingContact?.observe(viewLifecycleOwner) { id ->
            if (id != null) {
                contactId = id
                requestAddToContactList()
            }
        }
    }

    private fun requestAddToContactList() {
        viewModel.addToContactList(
            userId = viewModel.userId.value!!, token = viewModel.token.value!!,
            contactId = contactId!!,
            progressBar = viewModel.isVisibleProgressBarInFragmentAddContact)
    }

    private fun requestGetAllUsers() {
        viewModel.getAllUsers(token = viewModel.token.value!!,
            progressBar = viewModel.isVisibleProgressBarInFragmentAddContact)
    }

    private fun setListeners() {

        binding.toolbarAddContact.imgBackAddContact.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarAddContact.imgSearchAddContact.setOnClickListener {
            viewModel.isActiveSearchAddContact.value = true                                         // TODO - ВСТАВЛЯЮ ДАННЫЕ (меняю состояние)
        }

        binding.toolbarAddContact.imgCloseAddContact.setOnClickListener {
            viewModel.isActiveSearchAddContact.value = false                                        // TODO - ВСТАВЛЯЮ ДАННЫЕ (меняю состояние)
        }

        binding.toolbarAddContact.edSearchAddContact.doOnTextChanged { text, _, _, _ ->
            if(text?.isNotEmpty() == true ) {
                viewModel.filteredUserList.value?.clear()

                val filteredList = viewModel.listOfAllUsers.value?.filter {
                    it.name?.contains(text, true) == true
                }
                viewModel.filteredUserList.value = filteredList?.toMutableList()                    // TODO - ВСТАВЛЯЮ ДАННЫЕ
                actionAfterFiltered()
            } else {
                if (viewModel.listOfAllUsers.value?.isNotEmpty() == true)
                    viewModel.listOfAllUsers.value = viewModel.listOfAllUsers.value                 // TODO - ВСТАВЛЯЮ ДАННЫЕ
            }
        }
    }

    private fun actionAfterFiltered() {
        if (viewModel.filteredUserList.value?.isNotEmpty() == true) {
            binding.recyclerViewContacts.visibility = View.VISIBLE
            binding.noContactsContainer.visibility = View.GONE
            if (this::recyclerViewAdapter.isInitialized) {
                recyclerViewAdapter.submitList(viewModel.filteredUserList.value?.toList())
            } else {
                createAdapter(
                    multiSelectState = null,
                    usersInContactList = viewModel.userContactsIdList.toList(),
                    filteredContactsList = viewModel.filteredUserList.value?.toList())
            }
        } else {
            binding.recyclerViewContacts.visibility = View.GONE
            binding.noContactsContainer.visibility = View.VISIBLE

        }
    }
    private fun createConnectionErrorSnackBar(requestType: String): Snackbar {
        return Snackbar.make(binding.root,
            getString(R.string.connection_error_snackbar_message), Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(requireActivity().getColor(R.color.orange_color))
            .setAction(getString(R.string.connection_error_snackbar_action_button_text)) {
                when(requestType) {
                    RequestConst.REQUEST_GET_ALL_USERS -> { requestGetAllUsers() }
                    RequestConst.REQUEST_ADD_CONTACT -> { requestAddToContactList() }
                }
            }
    }

    private fun createAdapter(multiSelectState: Boolean?, usersInContactList: List<Int>?,
                              filteredContactsList: List<Contact>? = null) {
        recyclerViewAdapter = ContactAdapter(this, multiSelectState, usersInContactList, null)
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContacts.adapter = recyclerViewAdapter
        if (filteredContactsList == null)
            recyclerViewAdapter.submitList(viewModel.listOfAllUsers.value)
        else
            recyclerViewAdapter.submitList(filteredContactsList)
    }

    override fun onElementClickAction(contact: Contact) {
        val destinationPointWithData = FragmentAddContactDirections
            .actionFragmentAddContactToFragmentProfileInvite(contact)
        findNavController().navigate(destinationPointWithData)
    }

}