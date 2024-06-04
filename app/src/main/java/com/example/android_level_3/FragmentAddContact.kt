package com.example.android_level_3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_level_3.adapter.ContactAdapter
import com.example.android_level_3.adapter.ElementClickListener
import com.example.android_level_3.databinding.FragmentAddContactBinding
import com.example.android_level_3.retrofit.model.Contact
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar

class FragmentAddContact : Fragment(), ElementClickListener {

    private lateinit var binding: FragmentAddContactBinding
    private lateinit var recyclerViewAdapter: ContactAdapter
    private val viewModel: MainViewModel by activityViewModels()

    private val isVisibleProgressBar = MutableLiveData(false)
    private var connectionErrorSnackbar: Snackbar? = null
    private var contactId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddContactBinding.inflate(inflater, container, false)

        setObservers()
        setListeners()
        requestGetAllUsers()

        return binding.root
    }

    private fun setObservers() {

        viewModel.listOfAllUsers.observe(viewLifecycleOwner) {                         // отслеживание и отображение списка пользователей
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
            // при обновлении списка контактов, обновляем контакт-лист (для отображения добавленых)
            list.forEach {
                viewModel.userContactsIdList.add(it.id)
            }

            if(viewModel.isActiveSearchAddContact.value == false) {
                createAdapter(multiSelectState = null, usersInContactList = viewModel.userContactsIdList.toList())
            }
        }

        viewModel.addToContactListResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackbar = createConnectionErrorSnackBar(Const.REQUEST_ADD_CONTACT)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listWithAllContacts.value = serverResponse.body()?.data?.contacts
            } else {                                                                                // если неудачная авторизации (перезапускаем добавление контакта)
                connectionErrorSnackbar = createConnectionErrorSnackBar(Const.REQUEST_ADD_CONTACT)
                connectionErrorSnackbar?.show()
            }
        }

        viewModel.getAllUsersResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackbar = createConnectionErrorSnackBar(Const.REQUEST_GET_ALL_USERS)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listOfAllUsers.value = serverResponse.body()?.data?.users
            } else {                                                                                // если неудачная авторизации (перезапускаем)
                connectionErrorSnackbar = createConnectionErrorSnackBar(Const.REQUEST_GET_ALL_USERS)
                connectionErrorSnackbar?.show()
            }
        }

        isVisibleProgressBar.observe(viewLifecycleOwner) { visibility ->
            if (visibility) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        // показываем/прячем панель для поиска (при изменении состояния)
        viewModel.isActiveSearchAddContact.observe(viewLifecycleOwner) { visible ->
            if (visible) {
                with(binding){
                    toolbarAddContact.containerTextAddContact.visibility = View.GONE
                    toolbarAddContact.containerSearchAddContact.visibility = View.VISIBLE
                }
            } else {
                with(binding){
                    toolbarAddContact.containerTextAddContact.visibility = View.VISIBLE
                    toolbarAddContact.containerSearchAddContact.visibility = View.GONE
                    toolbarAddContact.edSearchAddContact.setText("")
                }
            }
        }

        // "слушатель" на добавление контакта
        val resultOfAddingContact =
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>(Const.RESULT_KEY)

        // наблюдатель за изменением состояния "слушателя"
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
            contactId = contactId!!, progressBar = isVisibleProgressBar)
    }

    private fun requestGetAllUsers() {
        viewModel.getAllUsers(token = viewModel.token.value!!, progressBar = isVisibleProgressBar)
    }

    private fun setListeners() {

        binding.toolbarAddContact.imgBackAddContact.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarAddContact.imgSearchAddContact.setOnClickListener {
            viewModel.isActiveSearchAddContact.value = true
        }

        binding.toolbarAddContact.imgCloseAddContact.setOnClickListener {
            viewModel.isActiveSearchAddContact.value = false
        }

        binding.toolbarAddContact.edSearchAddContact.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                if(s?.isNotEmpty() == true ) {
                    viewModel.filteredUserList.value?.clear()

                    val filteredList = viewModel.listOfAllUsers.value?.filter {
                        it.name?.contains(s, true) == true
                    }
                    viewModel.filteredUserList.value = filteredList?.toMutableList()
                    afterFilterAction()
                } else {
                    if (viewModel.listOfAllUsers.value?.isNotEmpty() == true)
                        viewModel.listOfAllUsers.value = viewModel.listOfAllUsers.value
                }
            }
        })
    }

    private fun afterFilterAction() {
        if (viewModel.filteredUserList.value?.isNotEmpty() == true) {
            binding.recyclerViewContacts.visibility = View.VISIBLE
            binding.noContactsContainer.visibility = View.GONE
            if (this::recyclerViewAdapter.isInitialized) {
                recyclerViewAdapter.submitList(viewModel.filteredUserList.value?.toList())
            } else {
                createAdapter( multiSelectState = null,
                    usersInContactList = viewModel.userContactsIdList.toList(),
                    filteredContactsList = viewModel.filteredUserList.value?.toList())
            }
        } else {
            binding.recyclerViewContacts.visibility = View.GONE
            binding.noContactsContainer.visibility = View.VISIBLE
        }

    }
    private fun createConnectionErrorSnackBar(requestType: String): Snackbar {
        return Snackbar.make(binding.root, "Problem with connection...", Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(requireActivity().getColor(R.color.orange_color))
            .setAction("RETRY") {
                when(requestType) {
                    Const.REQUEST_GET_ALL_USERS -> { requestGetAllUsers() }
                    Const.REQUEST_ADD_CONTACT -> { requestAddToContactList() }
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

    override fun onElementProfileClick(contact: Contact) { }
    override fun onElementLongClick(contactId: Int) { }
    override fun onElementChecked(checkBoxState: Boolean, contactId: Int) { }

}