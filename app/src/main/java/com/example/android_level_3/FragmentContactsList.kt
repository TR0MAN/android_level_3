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
import com.example.android_level_3.databinding.FragmentContactsListBinding
import com.example.android_level_3.retrofit.model.Contact
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar

class FragmentContactsList : Fragment() {

    private lateinit var binding: FragmentContactsListBinding
    private lateinit var recyclerViewAdapter: ContactAdapter
    private lateinit var actionListener: ElementClickListener

    private val viewModel: MainViewModel by activityViewModels()

    private var informationSnackbar: Snackbar? = null                                               // Snackbar для восстановления удаленного пользователя
    private var connectionErrorSnackbar: Snackbar? = null                                           // Snackbar для перезапуска запроса, при ошибке / долгом запросе
    private val isVisibleProgressBar = MutableLiveData(false)
    private var contactIdForDelete: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)

        setObservers()
        initElementClickListener()
        setFragmentButtonsListeners()

        if (viewModel.tabLayoutVisibility.value == true) {
            requestGetUserContacts()
        }

        return binding.root
    }

    // запрос списка контактов пользователя
    private fun requestGetUserContacts() {
        viewModel.getUserContacts(userId = viewModel.userId.value!!,
            token = viewModel.token.value!!, progressBar = isVisibleProgressBar)
    }

    // запрос на удаление из списка контактов
    private fun requestDeleteFromContacts() {
        viewModel.deleteFromContacts(
            userId = viewModel.userId.value!!, token = viewModel.token.value!!,
            idForDelete = contactIdForDelete!!, progressBar = isVisibleProgressBar)
    }

    // запрос на групповое удаление из списка контактов (по факту много раз одиночное)
    private fun deleteMultipleContact(contactsForGroupDelete: MutableSet<Int>?) {
        viewModel.deleteFromContacts( userId = viewModel.userId.value!!,
            token = viewModel.token.value!!, idForDelete = contactsForGroupDelete!!.first(),
            progressBar = isVisibleProgressBar)
    }

    // запрос на восстановление удаленного контакта
    private fun requestRestoreInContactList() {
        viewModel.addToContactList(userId = viewModel.userId.value!!, token = viewModel.token.value!!,
            contactId = contactIdForDelete!!, progressBar = isVisibleProgressBar)
    }

    // initializing observers
    private fun setObservers() {

        // observer for update List Adapter - (OK)
        viewModel.listWithAllContacts.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                binding.recyclerViewContacts.visibility = View.VISIBLE
                binding.noContactsContainer.visibility = View.GONE

                // актуализация списка контактов (их ID)
                viewModel.userContactsIdList.clear()
                list.forEach {
                    viewModel.userContactsIdList.add(it.id)
                }

                // восстановление состояния после поврота (список на удаление и отмеченные люди)
                if (viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                    createAdapter(multiSelectState = true,
                        selectedContacts = viewModel.listSelectedContactsForGroupDelete.value?.toList(),
                        contactsList = viewModel.listWithAllContacts.value)
                } else {
                    // восстановление состояния посика (если он был до поворота)
                    if (viewModel.isActiveSearchUserContacts.value == true) {
                        afterFilterAction()
                    } else {
                        // действия при обычном повороте
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

        // отслеживание результата запроса списка контактов
        viewModel.getUserContactsResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {                                                           // показываем Snackbar с ошибкой + можно перезапустить запрос
                connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_GET_CONTACTS)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listWithAllContacts.value = serverResponse.body()?.data?.contacts
            } else {
                connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_GET_CONTACTS)
                connectionErrorSnackbar?.show()
            }
        }

        // отслеживание результата запроса на удаление
        viewModel.deleteFromContactsResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {

                // вариант для группового удаления
                if(viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                    connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_GROUP_DELETE)
                    connectionErrorSnackbar?.show()
                } else {
                    connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_DELETE_CONTACT)
                    connectionErrorSnackbar?.show()
                }
            } else if (serverResponse.isSuccessful) {
                // вариант для группового удаления
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
                // случай с попыткой удалить контакт которого нет в списке контактов
                if (serverResponse.body()?.message.equals("Contact not found")) {
                    requestGetUserContacts()
                }
                // слечай для группового дуаления
                if (viewModel.listSelectedContactsForGroupDelete.value?.isEmpty() == false) {
                    connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_GROUP_DELETE)
                    connectionErrorSnackbar?.show()
                } else {
                    connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_DELETE_CONTACT)
                    connectionErrorSnackbar?.show()
                }
            }
        }

        // отслеживание результата запроса на добавление контакта в список
        viewModel.addToContactListResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_RESTORE_CONTACT)
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {
                viewModel.listWithAllContacts.value = serverResponse.body()?.data?.contacts
            } else {
                connectionErrorSnackbar = createConnectionSnackBar(Const.REQUEST_RESTORE_CONTACT)
                connectionErrorSnackbar?.show()
            }
        }

        isVisibleProgressBar.observe(requireActivity()) { visibility ->
            if (visibility) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        viewModel.isActiveSearchUserContacts.observe(viewLifecycleOwner) { visible ->
            if (visible) {
                with(binding){
                    toolbarContactList.containerTextContactList.visibility = View.GONE
                    toolbarContactList.containerSearchContactList.visibility = View.VISIBLE
                }
            } else {
                with(binding){
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

            override fun onElementClickAction(contact: Contact) {                                   // слушатель на удаление контакта
                contactIdForDelete = contact.id
                requestDeleteFromContacts()
                createInformationSnackbar()
                informationSnackbar?.show()
            }

            override fun onElementProfileClick(contact: Contact) {                                  // слушатель на переход к профилю контакта
                val destinationPointWithData = ViewPagerFragmentDirections
                    .actionViewPagerFragmentToFragmentContactProfile(contact)
                findNavController().navigate(destinationPointWithData)
                informationSnackbar?.dismiss()
            }

            override fun onElementLongClick(contactId: Int) {                                       // слушатель на переход в режим группового удаления
                viewModel.listSelectedContactsForGroupDelete.value =
                    viewModel.listSelectedContactsForGroupDelete.value?.apply { add(contactId) }
                createAdapter(multiSelectState = true,
                    selectedContacts = viewModel.listSelectedContactsForGroupDelete.value?.toList())
                viewModel.tabLayoutVisibility.value = false
            }

            override fun onElementChecked(checkBoxState: Boolean, contactId: Int) {                 // слушатель на клики по эелементу в режиме группового удаления
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
        informationSnackbar = Snackbar.make(binding.root, getString(R.string.snackbar_button_message), 5000)
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

        binding.toolbarContactList.edSearchContactList.addTextChangedListener(object : TextWatcher {    // слушатель на "поле ввода текста"
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                if(s?.isNotEmpty() == true ) {
                    viewModel.filteredContactsList.value?.clear()

                    viewModel.listWithAllContacts.value?.forEach { contact ->
                        if (contact.name?.contains(s, true) == true) {
                            viewModel.filteredContactsList.value =
                                viewModel.filteredContactsList.value?.apply { add(contact) }
                        }
                    }
                    afterFilterAction()
                } else {
                    if (viewModel.listWithAllContacts.value?.isNotEmpty() == true) {
                        viewModel.listWithAllContacts.value = viewModel.listWithAllContacts.value
                    }
                }
            }
        })

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

    // отображение результата поиска после фильтрации
    private fun afterFilterAction() {
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
        return Snackbar.make(binding.root, "Problem with connection...", Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(requireActivity().getColor(R.color.orange_color))
            .setAction("RETRY") {
                when(requestType){
                    Const.REQUEST_GET_CONTACTS -> { requestGetUserContacts() }
                    Const.REQUEST_DELETE_CONTACT -> { requestDeleteFromContacts() }
                    Const.REQUEST_RESTORE_CONTACT -> { requestRestoreInContactList() }
                    Const.REQUEST_GROUP_DELETE -> {
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
