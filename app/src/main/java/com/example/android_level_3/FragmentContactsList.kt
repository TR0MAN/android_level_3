package com.example.android_level_3

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_level_3.databinding.FragmentContactsListBinding
import com.example.android_level_3.utils.ext.toast
import com.google.android.material.snackbar.Snackbar

private const val SNACKBAR_VISIBILITY_KEY = "snackbarVisibility"

class FragmentContactsList : Fragment() {

    private lateinit var binding: FragmentContactsListBinding

    private val recyclerAdapter: ContactAdapter by lazy {
        ContactAdapter(actionListener)
    }
    private val viewModel: MainViewModel by viewModels()
    private val actionListener: ContactAdapter.ElementClickListener =
        object : ContactAdapter.ElementClickListener {
            override fun onElementDeleteClick(contact: Contact) {
                val position = viewModel.contactListLiveData.value?.indexOf(contact) ?: return
                if (position < 0) return

                viewModel.removeContact(contact)
                showContactRemovalSnackbar(contact, position)


//                if (snackbar != null) {
//                    viewModel.timerStop()
//                    snackbar = null
//                }
//                snackbar = createSnackbar(snackbarTimer)
//                snackbar?.show()
//                snackbarVisibility = true
//                viewModel.timerStart()
            }

            override fun onElementProfileClick(contact: Contact) {
                val destinationPointWithData = FragmentContactsListDirections
                    .actionFragmentContactsListToFragmentContactProfile(contact)
                findNavController().navigate(destinationPointWithData)
                snackbar?.dismiss()
            }
        }

    private var snackbar: Snackbar? = null
    private var snackbarVisibility = false
    private var snackbarTimer = 5
    private var temporaryContactData: Contact? = null
    private var temporaryContactPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // восстановление данных послеповорота экрана
        savedInstanceState?.let {
            snackbarVisibility = it.getBoolean(SNACKBAR_VISIBILITY_KEY)
            snackbarTimer = it.getInt(Const.SNACKBAR_TIMER_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)

        getResultFromCustomDialog()
        setObservers()
        setFragmentButtonsListeners()

        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = recyclerAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        // отображение SnackBar после поворота экрана (если он отображался до поворота)
//        if (snackbarVisibility) {
//            snackbar = createSnackbar(snackbarTimer)
//            snackbar?.show()
//            snackbarVisibility = true
//        }
//        showContactRemovalSnackbar()
    }

    private fun showContactRemovalSnackbar(contact: Contact, position: Int) {
        Snackbar.make(
            binding.root,
            "Contact was removed!",
            5000
        ).setAction("Cancel") {
            viewModel.addContact(contact, position)
        }.show()
    }

    // получение обьекта User с экрана добавления нового пользователя и добавление его в список
    @Suppress("DEPRECATION")
    private fun getResultFromCustomDialog() {
        parentFragmentManager.setFragmentResultListener(
            Const.REQUEST_KEY,
            viewLifecycleOwner
        ) { key, value ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                value.getSerializable(Const.RESULT_KEY, Contact::class.java)
                    ?.let { viewModel.addContact(it) }
            } else {
                viewModel.addContact(value.getSerializable(Const.RESULT_KEY) as Contact)
            }
        }
    }

    // инициализация наблюдателей за состоянием таймера обратного отсчета
    private fun setObservers() {
        // отображение сообщения на каждый тик (раз в 1 секунду)
        viewModel.onTickTimerMessage.observe(viewLifecycleOwner) { currentValue ->
            snackbar?.setText("Restore contact? ($currentValue sec)")
            snackbarTimer = currentValue
        }

        // по окончанию таймера, скрываем Snackbar
        viewModel.onFinishTimer.observe(viewLifecycleOwner) { status ->
            if (status) {
                snackbar?.let {
                    it.dismiss()
                    snackbar = null
                    snackbarVisibility = false
                }
            }
        }
        viewModel.contactListLiveData.observe(viewLifecycleOwner) { contacts ->
            recyclerAdapter.submitList(contacts.toMutableList())
        }
    }

//    }

    // создание и первичная инициализация Snackbar
//    private fun createSnackbar(currentTimer: Int): Snackbar? {
//        var snackbar: Snackbar? = Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT)
//        snackbar?.setText("Restore contact? ($currentTimer sec)")
//        snackbar?.setDuration(Snackbar.LENGTH_INDEFINITE)
//        snackbar?.setActionTextColor(requireContext().getColor(R.color.orange_color))
//        snackbar?.setAction(getString(R.string.snackbar_button_text)) {
//            snackbar?.dismiss()
//            viewModel.removeContact()
//            viewModel.timerStop()
//            snackbarVisibility = false
//            snackbar = null
//        }
    //        return snackbar

    // сохраняем состояние Snackbar (видимость и оставшееся время отсчета) при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SNACKBAR_VISIBILITY_KEY, snackbarVisibility)
        outState.putInt(Const.SNACKBAR_TIMER_KEY, snackbarTimer)
    }

    override fun onStart() {
        super.onStart()
        loadTemporaryDeletedData()
    }

    override fun onStop() {
        super.onStop()
        saveTemporaryDeletedData()
    }

    // инициализация слушателей кнопок фрагмента
    private fun setFragmentButtonsListeners() {

        binding.tvAddNewContact.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentContactsList_to_customDialog)
        }

        // реакция на "стрелочку" назад в ToolBar
        binding.toolbarContactList.imgBackToolbarContactList.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarContactList.imgSearchToolbarContactList.setOnClickListener {
            requireContext().toast(
                getString(R.string.toolbar_contact_list_search_button_toast_message)
            )
        }

    }

    // сохранение во ViewModel данных об удаленном пользователе
    private fun saveTemporaryDeletedData() {
        viewModel.temporaryContactData = temporaryContactData
        viewModel.temporaryUserPosition = temporaryContactPosition
    }

    // восстановление данных в RecyclerAdapter об удаленном пользователе
    private fun loadTemporaryDeletedData() {
        temporaryContactData = viewModel.temporaryContactData
        temporaryContactPosition = viewModel.temporaryUserPosition
    }
}
