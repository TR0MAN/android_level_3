package com.example.android_level_3

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.example.android_level_3.databinding.ActivityAddContactBinding

// Нужно делать через DialogFragment() из-за того, что он правильно управляет жизненным циклом диалога
// и восстанавливает его после поворота (диалог вовремя скрывется и правильно восстанавливается после поворота)
// при создании через AlertDialog.Builder после поворота диалог пропадет
class CustomDialog: DialogFragment() {

// TODO -> Возможна ли такая реализация слушателя во Fragment?
//    interface DialogButtonListener {
//        fun buttonSaveListener(newUser: User)
//    }

//  TODO -> если да, то как правильно сделать инициализацию в onAttach для Fragment? (так не работает)
//        try {
//            dialogListener = context/activity as DialogButtonListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException(activity.toString() + " must implement DialogButtonListener")
//        }


    private lateinit var addContactImageResult: ActivityResultLauncher<Intent>
    private val binding by lazy { ActivityAddContactBinding.inflate(layoutInflater) }
    private var avatarImageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // регистрация (выполнение) "контракта" после отработки startActivityForResult с получением данных (выбранной из галереи картинки)
        addContactImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                avatarImageUri = it?.data?.data

                binding.imgAvatar.setImageURI(avatarImageUri)
            }
        })
    }

    // восстанавливаем выбранную пользователем картинку (после поворота/пересоздания)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (avatarImageUri == null) {
            avatarImageUri = savedInstanceState?.getString(Const.AVATAR_IMAGE_KEY)?.toUri()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // вставляем картинку в ImageView (после поворота/пересоздания), если она есть
        avatarImageUri?.let { binding.imgAvatar.setImageURI(it) }

//        binding = ActivityAddContactBinding.inflate(LayoutInflater.from(context))                     (либо можно инициализировать и тут)
        var dialog: AlertDialog? = null
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setView(binding.root)

        // клик по кнопке "выбрать фото"
        binding.imgAddImageIcon.setOnClickListener {
            addContactImage()
        }

        // клик по кнопке SAVE
        binding.btnSaveUserData.setOnClickListener {
            parentFragmentManager.setFragmentResult(Const.REQUEST_KEY,
                Bundle().apply {
                    putSerializable(Const.RESULT_KEY, saveNewUserData())
                })
            dialog?.cancel()
        }

        // клик по стрелке "назад" в ToolBar
        binding.customToolbarAddContact.imgBackToolbarProfile.setOnClickListener {
            dialog?.cancel()
        }

        // иконка автоматического заполнения данными полей (для удобства теста)
        binding.imgFillFields.setOnClickListener { fillAllFields()  }

        dialog = dialogBuilder.create()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    // добавление картинки/аватарки нового контакта
    private fun addContactImage() {
        // работает с ACTION_OPEN_DOCUMENT и ACTION_GET_CONTENT
        val photoPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*" }
        addContactImageResult.launch(photoPickerIntent)
    }

    // обработка нажатия кнопки SAVE
    private fun saveNewUserData() :User {
        return User(
            userName = binding.etUserName.text.toString(),
            userCareer = binding.etCareer.text.toString(),
            userEmail = binding.etEmail.text.toString(),
            userPhoneNumber = binding.etPhone.text.toString(),
            userAddress = binding.etAddress.text.toString(),
            userBirthday = binding.etBirthday.text.toString(),
            userImage = avatarImageUri.toString()
        )
    }

    // автоматическое заполнение полей страницы с добавлением контакта, но можно и в ручную
    private fun fillAllFields() {
        binding.etUserName.setText("Roman")
        binding.etCareer.setText("Florist")
        binding.etEmail.setText("test_mail@gmail.com")
        binding.etPhone.setText("066-123-45-67")
        binding.etAddress.setText("Ukraine, Kiev")
        binding.etBirthday.setText("12/01/1990")
    }

    // сохраняем выбранную пользователем картинку (при повороте, до кнопки SAVE)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (avatarImageUri != null) {
            outState.putString(Const.AVATAR_IMAGE_KEY, avatarImageUri.toString())
        }
    }
}

