package com.example.android_level_3.authorization

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.android_level_3.Const
import com.example.android_level_3.MainActivity
import com.example.android_level_3.R
import com.example.android_level_3.databinding.ActivityRegistrationBinding
import com.example.android_level_3.retrofit.model.CreateUserModel
import com.example.android_level_3.retrofit.model.UserData
import com.example.android_level_3.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO семнить стартовую активити на AuthorizationActivity

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var addContactImageResult: ActivityResultLauncher<Intent>

    private var email: String? = null
    private var password: String? = null
    private var autoLogin: Boolean? = null

    private var isVisibleProgressBar = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent?.getStringExtra(Const.EMAIL) ?: "null_email"
        password = intent?.getStringExtra(Const.PASSWORD) ?: "null_pass"
        autoLogin = intent?.getBooleanExtra(Const.PREFERENCES_CHECKBOX, false)

        Log.d("TAG", "--- email = [$email]\n--- pass = [$password]\n--- checkbox = [$autoLogin]")

        setActivityResultContract()
        setEditTextListeners()

        // скрываем автоматически всплывающую клавиатуру
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

    }

    private fun setActivityResultContract() {
        // регистрация (выполнение) "контракта" после отработки startActivityForResult с получением данных (выбранной из галереи картинки)
        addContactImageResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                if (it.resultCode == RESULT_OK) {
                    val avatarImageUri = it?.data?.data
                    binding.imgAvatar.setImageURI(avatarImageUri)
                }
            })
    }

    private fun setEditTextListeners() {
        // hide and show HINT on click for PHONE field
        binding.etPhone.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    binding.etPhone.hint = " "
                } else {
                    if (binding.etPhone.text?.isEmpty() == true || binding.etPhone.text?.isBlank() == true)
                        binding.etPhone.hint = getString(R.string.dialog_add_contact_phone_hint)
                }
            }
        }
        // hide and show HINT on click for BIRTHDAY field
        binding.etBirthday.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    if (binding.etBirthday.text?.isEmpty() == true || binding.etBirthday.text?.isBlank() == true) {
                        binding.etBirthday.hint = " "
                        binding.tvBirthdayHelp.visibility = View.VISIBLE
                        binding.tvBirthdayHelp.setTextColor(getColor(R.color.orange_color))
                    }
                } else {
                    if (binding.etBirthday.text?.isEmpty() == true || binding.etBirthday.text?.isBlank() == true) {
                        binding.etBirthday.hint =
                            getString(R.string.dialog_add_contact_birthday_hint)
                        binding.tvBirthdayHelp.visibility = View.INVISIBLE
                    }
                }
            }
        }
        // check DATE format for BIRTHDAY field
        binding.etBirthday.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (Helper.dateValidator(s.toString()) == null) {
                    binding.tvBirthdayHelp.visibility = View.VISIBLE
                    binding.tvBirthdayHelp.setTextColor(Color.GREEN)
                    binding.tvBirthdayHelp.text = "( Ok )"
                } else {
                    binding.tvBirthdayHelp.visibility = View.VISIBLE
                    binding.tvBirthdayHelp.setTextColor(getColor(R.color.orange_color))
                    binding.tvBirthdayHelp.text = Helper.dateValidator(s.toString())
                }
            }
        })

        // choose avatar image
        binding.imgAddImageIcon.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*" }
            addContactImageResult.launch(photoPickerIntent)
        }

        // UPDATE user data after click button SAVE
        binding.btnSaveUserData.setOnClickListener {

            with(binding) {
                val image = null
                val name = Helper.dataFieldValidator(etUserName.text.toString()).also {
                    if (it == null) {
                        Toast.makeText(
                            this@RegistrationActivity,
                            getString(R.string.registration_empty_field_username),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }

                val career = Helper.dataFieldValidator(etCareer.text.toString())
                val phone = Helper.dataFieldValidator(etPhone.text.toString())
                val address = Helper.dataFieldValidator(etAddress.text.toString())
                val birthday = Helper.dataFieldValidator(etBirthday.text.toString())
                val date = birthday?.let {
                    if (Helper.dateValidator(it) != null) {
                        Toast.makeText(
                            this@RegistrationActivity,
                            getString(R.string.registration_wrong_birthday_format),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else {
                        Helper.getBirthday(it)
                    }
                }
                // TODO - DELETE after tests
                Log.d(
                    "TAG",
                    "name = $name \ncareer = $career \nphone = $phone \naddress = $address \nbirthday = $birthday [${birthday?.javaClass}] \ndate = $date [${date?.javaClass}] \nimage = $image"
                )

                isVisibleProgressBar.value = true

                // Запускаем процесс создания НОВОГО пользователя (имея все данные)
                CoroutineScope(Dispatchers.IO).launch {
                    // иммитация загрузки
                    delay(2000)

                    val serverResponse = viewModel.serverApi.registerNewUser(
                        CreateUserModel(
                            email = email!!,
                            password = password!!,
                            name = name.toString(),
                            phone = phone.toString(),
                            address = address.toString(),
                            career = career.toString(),
                            birthday = date,
                            image = null
                        )
                    )

                    if (serverResponse.isSuccessful) {
                        // выключаем progressBar
                        withContext(Dispatchers.Main){
                            isVisibleProgressBar.postValue(false)
                        }

                        // сохраняем данные о авторизации
                        saveUserDataToPreferences(serverResponse.body()?.data)

                        //переходим на ПРОФИЛЬ (там данные получим из ViewModel)
                        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        startActivity(intent)
                        Log.d("TAG", "ServerResponse = OK")
                        Log.d("TAG", "ServerResponse.message = [${serverResponse.message()}]")
                        Log.d("TAG", "ServerResponse.body()?.message = [${serverResponse.body()?.message}]")
                        Log.d("TAG", "ServerResponse.body()?.status = [${serverResponse.body()?.status}]")

                    } else {
                        // реакция на ошибку (нет связи или еще что-то)
                        Log.d("TAG", "ServerResponse = ERROR")
                        Log.d("TAG", "ServerResponse.message = [${serverResponse.message()}]")

                        withContext(Dispatchers.Main){
                            isVisibleProgressBar.postValue(false)
                        }

                    }
                }
            }
        }

        // TODO - only for quick test (DELETE after tests)
        binding.imgFillAllDataFields.setOnClickListener {
            with(binding) {
                etUserName.setText("Sergey Nenulov")
                etCareer.setText("Engineer")
                etPhone.setText("050-111-22-33")
                etAddress.setText("Ukraine, Kiev, Victory street, 1")
                etBirthday.setText("2000/03/16")
            }
        }
    }

    private fun saveUserDataToPreferences(responseData: UserData?) {
        val sharedPreferences = getSharedPreferences(Const.PREFERENCES_SETTINGS, MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(Const.PREFERENCES_AUTHORISATION_TOKEN, responseData?.accessToken)
            putString(Const.PREFERENCES_REFRESH_TOKEN, responseData?.refreshToken)
            putString(Const.PREFERENCES_USER_NAME, responseData?.user?.name)
            putString(Const.PREFERENCES_USER_CAREER, responseData?.user?.career)
            putString(Const.PREFERENCES_USER_ADDRESS, responseData?.user?.address)
            if (autoLogin == true) {
                putString(Const.PREFERENCES_EMAIL, email)
                putString(Const.PREFERENCES_PASSWORD, password)
            }
        }.apply()
    }

}