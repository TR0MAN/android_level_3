package com.example.android_level_3.authorization

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.Const
import com.example.android_level_3.MainActivity
import com.example.android_level_3.R
import com.example.android_level_3.databinding.ActivityAuthorizationBinding
import com.example.android_level_3.retrofit.model.ServerResponse
import com.example.android_level_3.retrofit.model.UserAuthorisationEntity
import com.example.android_level_3.retrofit.model.UserData
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.internal.wait
import retrofit2.Response
import java.net.ConnectException
import kotlin.random.Random

// 1. Работает ЛОГИН
// 2. Переход на 2-й экран
// 3. Создание нового пользователя
// 4. Переход на экран ПРОФИЛЯ с ЛОГИНА или РЕГИСТРАЦИИ
// 5. Работает галочка "запомнить", переходит на экран ПРОФИЛЬ (если была отмечена)
// 6. Передаются данные на экран ПРОФИЛЯ (через SharedPreferences), данные подставляются в поля
// 7. Работает кнопка "ВЫХОД", удаляются данные из SharedPreferences
// 8. добавлен диалог для регистрации при плохом ответе от сервера
// 9. Сделан autoLogin, обновление токена - через авторизацию (заново)
// 10. Перенесена реализация автологина/обновления данных в ПРОФИЛЬ
// 11. Отлавливается отсутствие И-нета и дается возможность повторить попытку авторизации
// 12. Есть проверка на длительное время получение ответа от сервера, есть возможность повторить попытку авторизации
// TODO - получать данные о контактах USERa, делать добавление/удаление
// TODO - подумать над отдельным классом для методов запроса к серверу
// TODO - подумать над отдельным диалоговым окном для обновление данных в ПРОФИЛЕ


class AuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorizationBinding
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var viewModel: MainViewModel

    private var isVisibleProgressBar = MutableLiveData(false)
    private var authorisationAction = MutableLiveData<Response<ServerResponse>?>()
    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        sharedPreferences = getSharedPreferences(Const.PREFERENCES_SETTINGS, MODE_PRIVATE)
        autoLoginCheck()

        setObservers()

        customSymbolTextInputForm()
        emailFormObserver()
        passwordFormObserver()
        registration()
    }

    private fun setObservers() {
        isVisibleProgressBar.observe(this) {
            if (it) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        authorisationAction.observe(this) { serverResponse ->

            // если пришел NULL, показываем Snackbar с ошибкой и даем возможность перезапустить
            if (serverResponse == null) {
                snackBar = createSnackBar()
                snackBar?.show()

            } else if (serverResponse.isSuccessful) {

                // убираем ProgressBar и переходим в ПРОФИЛЬ
                // ответ ОК (такой USER ЕСТЬ)
                // пришли данные о USER + AccessToken и RefreshToken
                Log.d("TAG", "ServerResponse = OK")
                Log.d("TAG", "ServerResponse.message = [${serverResponse.message()}]")
                Log.d("TAG", "ServerResponse.body()?.message = [${serverResponse.body()?.message}]")
                Log.d("TAG", "ServerResponse.body()?.status = [${serverResponse.body()?.status}]")
                Log.d("TAG","ServerResponse.body()?.data?.accessToken = [${serverResponse.body()?.data?.accessToken}]")
                bodyToLog(serverResponse.body()?.data)      // для логирования                      (TODO - DELETE AFTER TEST)

                // запоминаю данные в SharedPreferences для автологина (если была галочка)
                checkingAutoLoginFunction()

                // сохраняем пришедшие данные AccessToken и RefreshToken и UserData
                saveUserDataToPreferences(serverResponse.body()?.data)

                // переходим в ПРОФИЛЬ
                val intent = Intent(this@AuthorizationActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.horiz_from_right_to_center, R.anim.horiz_from_center_to_left)


            } else {

                Log.d("TAG", "ServerResponse = ERROR")
                Log.d("TAG", "ServerResponse.message = [${serverResponse.message()}]")
                Log.d("TAG", "ServerResponse.code = [${serverResponse.code()}]")
                Log.d("TAG", "ServerResponse.errorBody = [${serverResponse.errorBody()}]")

                // ответ ERROR (такого пользователя НЕТ)
                createAlertDialog().show()
            }
        }
    }




    // проверка, нет ли сохраненных записей по ключу "email"
    // если есть, достаем данные (адрес почты/пароль) по ключу и переходим сразу на страницу профиля
    // с подстановкой данных взятых из SharedPreferences
    private fun autoLoginCheck() {
        Log.d("TAG", "INSIDE - autoLoginCheck")
        Log.d("TAG","FRAGMENT[email]= ${sharedPreferences?.getString(Const.PREFERENCES_EMAIL, "null")}")
        Log.d("TAG","FRAGMENT[pass]= ${sharedPreferences?.getString(Const.PREFERENCES_PASSWORD, "null")}" )
        Log.d("TAG","FRAGMENT[access_token]= ${sharedPreferences?.getString(Const.PREFERENCES_AUTHORISATION_TOKEN,"null")}")
        Log.d("TAG","FRAGMENT[ref_token]= ${sharedPreferences?.getString(Const.PREFERENCES_REFRESH_TOKEN,"null")}")

        // TODO - подумать про перенос в ПРОФИЛЬ

        if (sharedPreferences?.contains(Const.PREFERENCES_EMAIL) == true) {
            sharedPreferences?.edit()?.apply {
                putBoolean(Const.PREFERENCES_AUTOLOGIN, true)
            }?.apply()

            startActivity(Intent(this, MainActivity::class.java))

//            CoroutineScope(Dispatchers.IO).launch {
//                // запрос авторизации по LOGIN и PASS
//                authorizationRequest(
//                    email = sharedPreferences?.getString(Const.PREFERENCES_EMAIL, "null").toString(),
//                    password = sharedPreferences?.getString(Const.PREFERENCES_PASSWORD, "null").toString()
//                )
//            }
//            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // переход к MainActivity после удачной регистрации
    private fun registration() {
        binding.btnAuthorizationRegister.setOnClickListener {

            // TODO кнопка регистрации
            if (emailValidator() && passwordValidator() == getString(R.string.response_ok)) {

//                isVisibleProgressBar.value = true
                CoroutineScope(Dispatchers.IO).launch {
                    // запрос авторизации по LOGIN и PASS
                    authorizationRequest(
                        email = binding.textInputEmailForm.text.toString(),
                        password = binding.textInputPasswordForm.text.toString()
                    )
                }
            } else {
                Toast.makeText(this,
                    getString(R.string.empty_password_or_email_fields), Toast.LENGTH_SHORT).show()
            }
        }

        //TODO - Only for TEST (DELETE)
        binding.imgFillAuthorisationData.setOnClickListener {
            fillAuthorisationData()
        }
    }

    private suspend fun authorizationRequest(email: String, password: String) {
        Log.d("TAG", "FUN authorizationRequest() -> INSIDE")

        // показываем ProgressBar
        isVisibleProgressBar.postValue(true)

        // если запрос дольше 5 сек, возвращаем NULL
        val job = withTimeoutOrNull(5000L) {
            try {
                val serverResponse = viewModel.serverApi.authoriseUser(
                    UserAuthorisationEntity(
                        email = email,
                        password = password
                    )
                )
                delay(2000L)     // иммитация запроса (DELETE)
                authorisationAction.postValue(serverResponse)
            } catch (e: ConnectException) {                         // ловим проблему с отключенным И-нетом и перезапускаем
                authorisationAction.postValue(null)
            }
        }
        // если долгий запрос, возвращаем NULL и пробуем перезапустить
        if (job == null) {
            authorisationAction.postValue(null)
        }
        // убираем ProgressBar
        isVisibleProgressBar.postValue(false)
    }

    private fun saveUserDataToPreferences(responseData: UserData?) {
        sharedPreferences?.edit()?.apply {
            putString(Const.PREFERENCES_AUTHORISATION_TOKEN, responseData?.accessToken)
            putString(Const.PREFERENCES_REFRESH_TOKEN, responseData?.refreshToken)
            putString(Const.PREFERENCES_USER_NAME, responseData?.user?.name)
            putString(Const.PREFERENCES_USER_CAREER, responseData?.user?.career)
            putString(Const.PREFERENCES_USER_ADDRESS, responseData?.user?.address)
        }?.apply()
    }

    //TODO - Only for TEST (DELETE)
    private fun bodyToLog(userData: UserData?) {
        Log.d("TAG", "id = [${userData?.user?.id}]")
        Log.d("TAG", "name = [${userData?.user?.name}]")
        Log.d("TAG", "email = [${userData?.user?.email}]")
        Log.d("TAG", "phone = [${userData?.user?.phone}]")
        Log.d("TAG", "career = [${userData?.user?.career}]")
        Log.d("TAG", "address = [${userData?.user?.address}]")
        Log.d("TAG", "birthday = [${userData?.user?.birthday}]")
        Log.d("TAG", "facebook = [${userData?.user?.facebook}]")
        Log.d("TAG", "instagram = [${userData?.user?.instagram}]")
        Log.d("TAG", "twitter = [${userData?.user?.twitter}]")
        Log.d("TAG", "linkedin = [${userData?.user?.linkedin}]")
        Log.d("TAG", "image = [${userData?.user?.image}]")

    }

    // проверка отмечена ли галочка "Remember Me", запись/удаление данных в sharedPreferences
    private fun checkingAutoLoginFunction() {
        if (binding.checkBoxAuthorizationRememberMe.isChecked) {
            sharedPreferences?.edit()?.apply {
                putString(Const.PREFERENCES_EMAIL, binding.textInputEmailForm.text.toString())
                putString(Const.PREFERENCES_PASSWORD, binding.textInputPasswordForm.text.toString())
            }?.apply()
        }
    }

    private fun createAlertDialog(): AlertDialog.Builder {
        return AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.authorisation_alert_dialog_title))
            setMessage(getString(R.string.authorisation_alert_dialog_help_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.authorisation_alert_dialog_positive_button_text)) { dialog, id ->
                // ЕСЛИ РЕГИСТРАЦИЯ
                val intent = Intent(this@AuthorizationActivity, RegistrationActivity::class.java)
                // Передаем ВСЕ данные о пользователе (ПОЧТА/ПАС)
                intent.putExtra(Const.EMAIL, binding.textInputEmailForm.text.toString())
                intent.putExtra(Const.PASSWORD, binding.textInputPasswordForm.text.toString())
                if (binding.checkBoxAuthorizationRememberMe.isChecked) {
                    intent.putExtra(Const.PREFERENCES_CHECKBOX, true)
                } else {
                    intent.putExtra(Const.PREFERENCES_CHECKBOX, false)
                }
                startActivity(intent)
                dialog.cancel()
                overridePendingTransition(R.anim.horiz_from_right_to_center, R.anim.horiz_from_center_to_left)
            }
            setNegativeButton(getString(R.string.authorisation_alert_dialog_negative_button_text)) { dialog, id ->
                dialog.cancel()
            }
        }
    }

    // отслеживание изменений в поле e-mail, с последующей валидацией
    private fun emailFormObserver() {
        binding.textInputEmailForm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!emailValidator()) {binding.textInputEmailContainer.helperText =
                        getString(R.string.response_wrong_email)
                    binding.textInputEmailContainer.setHelperTextColor(ColorStateList
                            .valueOf(getColor(R.color.orange_color))
                    )
                } else {
                    binding.textInputEmailContainer.helperText = getString(R.string.response_ok)
                    binding.textInputEmailContainer.setHelperTextColor(ColorStateList.valueOf(Color.GREEN))
                }
            }
        })
    }

    // валидация введенного текста, для почты
    private fun emailValidator(): Boolean {
        val email = binding.textInputEmailForm.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        return true
    }

    // отслеживание изменений в поле password, с последующей валидацией
    private fun passwordFormObserver() {
        binding.textInputPasswordForm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (passwordValidator() != getString(R.string.response_ok)) {
                    binding.textInputPasswordContainer.helperText = passwordValidator()
                    binding.textInputPasswordContainer.setHelperTextColor(
                        ColorStateList
                            .valueOf(getColor(R.color.orange_color))
                    )
                } else {
                    binding.textInputPasswordContainer.helperText = passwordValidator()
                    binding.textInputPasswordContainer
                        .setHelperTextColor(ColorStateList.valueOf(Color.GREEN))
                }
            }
        })
    }

    // валидация введенного текста, для почты
    private fun passwordValidator(): String {
        val password = binding.textInputPasswordForm.text.toString()
        if (password.length < 8) {
            return getString(R.string.error_min_8_symbols_password)
        }
        if (!password.matches(".*[A-Z].*".toRegex())) {
            return getString(R.string.error_upper_case_symbol)
        }
        if (!password.matches(".*[a-z].*".toRegex())) {
            return getString(R.string.error_lower_case_symbol)
        }
        if (!password.matches(".*[0-9].*".toRegex())) {
            return getString(R.string.error_contain_number)
        }
        if (!password.matches(".*[!@#\$%^&*].*".toRegex())) {
            return getString(R.string.error_contain_special_symbol)
        }
        if (password.matches(".*[~`()_+|\\?/.{}\\[\\],<>=\\-].*".toRegex())) {
            return getString(R.string.error_include_wrong_spec_symbols)
        }
        if (password.matches(".*[ ].*".toRegex())) {
            return getString(R.string.error_include_white_space)
        }
        return getString(R.string.response_ok)
    }


    // замена стандартного символа скрытия буквы пароля, на большой символ ('●' '⬤')
    private fun customSymbolTextInputForm() {
        binding.textInputPasswordForm.transformationMethod =
            object : PasswordTransformationMethod() {
                override fun getTransformation(source: CharSequence, view: View): CharSequence {
                    val transformation = super.getTransformation(source, view)
                    return object : CharSequence by transformation {
                        override fun get(index: Int): Char {
                            return if (transformation[index] == '\u2022') {
                                Const.DOT
                            } else {
                                transformation[index]
                            }
                        }
                    }
                }
            }
    }

    // сохраняем состояние полей ввода и checkbox
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString(Const.STATE_EMAIL_FIELD, binding.textInputEmailForm.text.toString())
            putString(Const.STATE_PASSWORD_FIELD, binding.textInputPasswordForm.text.toString())
            putBoolean(Const.STATE_CHECKBOX, binding.checkBoxAuthorizationRememberMe.isChecked)
        }
        super.onSaveInstanceState(outState)
    }

    // восстанавливаем состояние полей ввода и checkbox
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            binding.textInputEmailForm.setText(getString(Const.STATE_EMAIL_FIELD))
            binding.textInputPasswordForm.setText(getString(Const.STATE_PASSWORD_FIELD))
            binding.checkBoxAuthorizationRememberMe.isChecked = getBoolean(Const.STATE_CHECKBOX)
        }
    }

    private fun createSnackBar(): Snackbar {
        return Snackbar.make(binding.root, "Problem with connection...", Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(getColor(R.color.orange_color))
            .setAction("RETRY") {

                CoroutineScope(Dispatchers.IO).launch {
                    authorizationRequest(
                        email = binding.textInputEmailForm.text.toString(),
                        password = binding.textInputPasswordForm.text.toString()
                    )
                }
            }
    }

    fun fillAuthorisationData() {
        binding.textInputEmailForm.setText("unit5@email.com")
        binding.textInputPasswordForm.setText("1!Qqwerty")
    }

    override fun onStop() {
        super.onStop()
        snackBar?.dismiss()
    }

}

