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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_level_3.adapter.ContactAdapter
import com.example.android_level_3.adapter.ContactAdapter2
import com.example.android_level_3.adapter.ElementClickListener
import com.example.android_level_3.databinding.FragmentContactsListBinding
import com.example.android_level_3.model.Contact
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import java.text.FieldPosition
import kotlin.concurrent.thread

class FragmentContactsList : Fragment() {

    private lateinit var binding: FragmentContactsListBinding
    private lateinit var recyclerAdapter: ContactAdapter

    private lateinit var recyclerListAdapter: ContactAdapter2

    private val viewModel: MainViewModel by viewModels()

    private lateinit var actionListener: ElementClickListener
    private var snackbar: Snackbar? = null
    private var snackbarVisibility = false
    private var snackbarTimer = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // восстановление данных послеповорота экрана
        savedInstanceState?.let {
            snackbarVisibility = it.getBoolean(Const.SNACKBAR_VISIBILITY_KEY)
            snackbarTimer = it.getInt(Const.SNACKBAR_TIMER_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsListBinding.inflate(inflater, container, false)

        getResultFromCustomDialog()
        initCountDownListeners()
        initElementClickListener()
        setFragmentButtonsListeners()

//        recyclerAdapter = ContactAdapter(viewModel.getContactList(), actionListener)
//        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvContacts.adapter = recyclerAdapter

        recyclerListAdapter = ContactAdapter2(actionListener)
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = recyclerListAdapter

//        recyclerListAdapter.submitList(viewModel.getContactList())      COMMENT

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // отображение SnackBar после поворота экрана (если он отображался до поворота)
        if (snackbarVisibility) {
            snackbar = createSnackbar(snackbarTimer)
            snackbar?.show()
            snackbarVisibility = true
        }
    }

    // получение обьекта User с экрана добавления нового пользователя и добавление его в список
    private fun getResultFromCustomDialog() {
        parentFragmentManager.setFragmentResultListener(Const.REQUEST_KEY, viewLifecycleOwner){ key, value ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                value.getSerializable(Const.RESULT_KEY, Contact::class.java)
                    ?.let {
//                        recyclerAdapter.addNewContact(it)       // TODO добавляю новый контакт (исправить ID)

                        // TODO - ADD CONTACT
                        viewModel.addContact(it)
                    }
            } else {
//                recyclerAdapter.addNewContact(value.getSerializable(Const.RESULT_KEY) as Contact)

                // TODO - ADD CONTACT
                viewModel.addContact(value.getSerializable(Const.RESULT_KEY) as Contact)

                // только для теста, что обновлять список
//                recyclerAdapter.notifyDataSetChanged()
            }
        }
    }

    // инициализация наблюдателей за состоянием таймера обратного отсчета
    private fun initCountDownListeners() {
        // отображение сообщения на каждый тик (раз в 1 секунду)
        viewModel.onTickTimerMessage.observe(viewLifecycleOwner) { currentValue ->
            snackbar?.setText("Restore contact? ($currentValue sec)")
            snackbarTimer = currentValue
        }

        // по окончанию таймера, скрываем Snackbar
        viewModel.onFinishTimer.observe(viewLifecycleOwner) {status ->
            if (status) {
                snackbar?.let {
                    it.dismiss()
                    snackbar = null
                    snackbarVisibility = false
                }
            }
        }


        // обновление списка в List Adapter
        viewModel.observableContactList.observe(viewLifecycleOwner) { list ->
                list.forEach {
                    Log.d("TAG", "[FRAGMENT] > contact = $it")
                }
                Log.d("TAG", "[FRAGMENT] > List Size = ${list.size}")
                Log.d("TAG", "[FRAGMENT] > LastContact = ${list.last()}")

            recyclerListAdapter.submitList(list)
        }



    }

    // инициализация слушателя нажатий по элементам списка
    private fun initElementClickListener() {

        actionListener = object : ElementClickListener {

            override fun onElementDeleteClick(position: Int) {
//                recyclerAdapter.removeContact(position)

                Log.d("TAG", "contact = $position")
//                // TODO - DELETE CONTACT
                viewModel.deleteContact(position)

//                recyclerListAdapter.submitList(viewModel.getContactList())      // TODO - ???

//                recyclerAdapter.notifyDataSetChanged()              // только для теста, что обновлять список

                if (snackbar != null) {
                    viewModel.timerStop()
                    snackbar = null
                }
                snackbar = createSnackbar(snackbarTimer)
                snackbar?.show()
                snackbarVisibility = true
                viewModel.timerStart()
            }

            override fun onElementProfileClick(position: Int) {
//                val currentUser = viewModel.getContactList()[position]            COMMENT
//                val destinationPointWithData = FragmentContactsListDirections
//                    .actionFragmentContactsListToFragmentContactProfile(currentUser)
//                findNavController().navigate(destinationPointWithData)
                snackbar?.dismiss()
            }
        }
    }

    // создание и первичная инициализация Snackbar
    private fun createSnackbar(currentTimer: Int): Snackbar? {
        val rootView = binding.clFragmentMainBasicLayout as View
        var snackbar: Snackbar? = Snackbar.make(rootView,"", Snackbar.LENGTH_SHORT)
        snackbar?.setText("Restore contact? ($currentTimer sec)")
        snackbar?.setDuration(Snackbar.LENGTH_INDEFINITE)
        snackbar?.setActionTextColor(requireContext().getColor(R.color.orange_color))
        snackbar?.setAction(getString(R.string.snackbar_button_text)) {
            snackbar?.dismiss()

//            recyclerAdapter.restoreContact()

            // TODO RESTORE CONTACT
            viewModel.restoreContact()
//            recyclerListAdapter.submitList(viewModel.getContactList())        COMMENT
//            recyclerAdapter.notifyDataSetChanged()                  // только для теста, что обновлять список


            viewModel.timerStop()
            snackbarVisibility = false
            snackbar = null
        }
        return snackbar
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
            Toast.makeText(requireContext(),
                getString(R.string.toolbar_contact_list_search_button_toast_message), Toast.LENGTH_SHORT).show()
        }

    }

    // сохраняем состояние Snackbar (видимость и оставшееся время отсчета) при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(Const.SNACKBAR_VISIBILITY_KEY, snackbarVisibility)
        outState.putInt(Const.SNACKBAR_TIMER_KEY, snackbarTimer)
    }


    // сохранение во ViewModel данных об удаленном пользователе
//    private fun saveTemporaryDeletedData() {
//        viewModel.deletedContactData = recyclerAdapter.temporaryContactData
//        viewModel.deletedContactPosition = recyclerAdapter.temporaryContactPosition
//    }

    // восстановление данных в RecyclerAdapter об удаленном пользователе
//    private fun loadTemporaryDeletedData() {
//        recyclerAdapter.temporaryContactData = viewModel.deletedContactData
//        recyclerAdapter.temporaryContactPosition = viewModel.deletedContactPosition
//    }

//    override fun onStart() {
//        super.onStart()
//        loadTemporaryDeletedData()
//    }

//    override fun onStop() {
//        super.onStop()
//        saveTemporaryDeletedData()
//    }

}
