package com.example.android_level_3.authorization

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.Const
import com.example.android_level_3.MainActivity
import com.example.android_level_3.R
import com.example.android_level_3.databinding.ActivityAuthorizationBinding
import com.example.android_level_3.retrofit.model.UserData
import com.example.android_level_3.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar

class AuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorizationBinding
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var viewModel: MainViewModel

    private var isVisibleProgressBar = MutableLiveData(false)
    private var connectionErrorSnackBar: Snackbar? = null

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
        setListeners()
    }

    private fun setObservers() {
        isVisibleProgressBar.observe(this) {
            if (it) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        viewModel.authorisationResult.observe(this) { serverResponse ->
            if (serverResponse == null) {                                                           // ошибка при отключенном Интернете или долгом запросе
                connectionErrorSnackBar = createSnackBar()
                connectionErrorSnackBar?.show()
            } else if (serverResponse.isSuccessful) {
                // запоминаю данные в SharedPreferences для автологина (если была галочка)
                checkingAutoLoginFunction()
                // сохраняем пришедшие данные AccessToken, RefreshToken и UserData
                saveUserDataToPreferences(serverResponse.body()?.data)
                // переходим в профиль пользователя
                val intent = Intent(this@AuthorizationActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.horiz_from_right_to_center,
                    R.anim.horiz_from_center_to_left)
            } else {
                // если пользователя с введенными e-mail и password нет, предлагаем создать
                createAlertDialog().show()
            }
        }
    }

    // проверка, нет ли сохраненных записей по ключу "email"
    // если есть, ставим "метку" что нужен автологин и переходим на страницу профиля
    private fun autoLoginCheck() {
        if (sharedPreferences?.contains(Const.PREFERENCES_EMAIL) == true) {
            sharedPreferences?.edit()?.apply {
                putBoolean(Const.PREFERENCES_AUTOLOGIN, true)
            }?.apply()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // переход к MainActivity после удачной регистрации
    private fun setListeners() {
        binding.btnAuthorizationRegister.setOnClickListener {
            if (emailValidator() && passwordValidator() == getString(R.string.response_ok)) {

                viewModel.getAuthorisation( email = binding.textInputEmailForm.text.toString(),
                    password = binding.textInputPasswordForm.text.toString(),
                    progressBar = isVisibleProgressBar)
            } else {
                Toast.makeText(this,
                    getString(R.string.empty_password_or_email_fields), Toast.LENGTH_SHORT).show()
            }
        }

        // TODO - Only for TEST (DELETE)
        binding.imgFillAuthorisationData.setOnClickListener {
            fillAuthorisationData()
        }
    }

    private fun saveUserDataToPreferences(responseData: UserData?) {
        sharedPreferences?.edit()?.apply {
            putString(Const.PREFERENCES_ACCESS_TOKEN, responseData?.accessToken)
            putString(Const.PREFERENCES_REFRESH_TOKEN, responseData?.refreshToken)
            putString(Const.PREFERENCES_USER_NAME, responseData?.user?.name)
            putString(Const.PREFERENCES_USER_CAREER, responseData?.user?.career)
            putString(Const.PREFERENCES_USER_ADDRESS, responseData?.user?.address)
            putInt(Const.PREFERENCES_USER_ID, responseData?.user?.id!!)
        }?.apply()
    }

    // если выбран "Remember Me", запись данных для авторизации в SharedPreferences
    private fun checkingAutoLoginFunction() {
        if (binding.checkBoxAuthorizationRememberMe.isChecked) {
            sharedPreferences?.edit()?.apply {
                putString(Const.PREFERENCES_EMAIL, binding.textInputEmailForm.text.toString())
                putString(Const.PREFERENCES_PASSWORD, binding.textInputPasswordForm.text.toString())
            }?.apply()
        }
    }

    // диалоговое окно для создания нового пользователя
    private fun createAlertDialog(): AlertDialog.Builder {
        return AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.authorisation_alert_dialog_title))
            setMessage(getString(R.string.authorisation_alert_dialog_help_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.authorisation_alert_dialog_positive_button_text)) { dialog, id ->
                val intent = Intent(this@AuthorizationActivity, RegistrationActivity::class.java)
                // передаем почту, пароль, состояние checkBox для регистрации нового пользователя
                    intent.putExtra(Const.EMAIL, binding.textInputEmailForm.text.toString())
                    intent.putExtra(Const.PASSWORD, binding.textInputPasswordForm.text.toString())
                if (binding.checkBoxAuthorizationRememberMe.isChecked) {
                    intent.putExtra(Const.PREFERENCES_CHECKBOX, true)
                } else {
                    intent.putExtra(Const.PREFERENCES_CHECKBOX, false)
                }
                startActivity(intent)
                dialog.cancel()
                overridePendingTransition(R.anim.horiz_from_right_to_center,
                    R.anim.horiz_from_center_to_left)
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
                    binding.textInputEmailContainer.setHelperTextColor( ColorStateList
                            .valueOf(getColor(R.color.orange_color)) )
                } else {
                    binding.textInputEmailContainer.helperText = getString(R.string.response_ok)
                    binding.textInputEmailContainer.setHelperTextColor(ColorStateList.valueOf(Color.GREEN))
                }
            }
        })
    }

    // валидация введенного текста (для почты)
    private fun emailValidator(): Boolean {
        val email = binding.textInputEmailForm.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        return true
    }

    // отслеживание поля для введения пароля, с последующей валидацией
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

    // сохраняем состояние полей ввода и checkbox (при повороте)
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString(Const.STATE_EMAIL_FIELD, binding.textInputEmailForm.text.toString())
            putString(Const.STATE_PASSWORD_FIELD, binding.textInputPasswordForm.text.toString())
            putBoolean(Const.STATE_CHECKBOX, binding.checkBoxAuthorizationRememberMe.isChecked)
        }
        super.onSaveInstanceState(outState)
    }

    // восстанавливаем состояние полей ввода и checkbox (после поворота)
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            binding.textInputEmailForm.setText(getString(Const.STATE_EMAIL_FIELD))
            binding.textInputPasswordForm.setText(getString(Const.STATE_PASSWORD_FIELD))
            binding.checkBoxAuthorizationRememberMe.isChecked = getBoolean(Const.STATE_CHECKBOX)
        }
    }

    // информационное сообщение о проблемах с Интернетом или долгий ответ сервера, с перезапуском
    private fun createSnackBar(): Snackbar {
        return Snackbar.make(binding.root, "Problem with connection...", Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(getColor(R.color.orange_color))
            .setAction("RETRY") {
                viewModel.getAuthorisation( email = binding.textInputEmailForm.text.toString(),
                    password = binding.textInputPasswordForm.text.toString(),
                    progressBar = isVisibleProgressBar)
            }
    }

    // метод автоматического заполнения полей почта и пароль (для быстрого теста) (УДАЛИТЬ)
    private fun fillAuthorisationData() {
        binding.textInputEmailForm.setText("unit6@email.com")
        binding.textInputPasswordForm.setText("2@Qwertyu")

        // старый контакт
//        binding.textInputEmailForm.setText("unit5@email.com")
//        binding.textInputPasswordForm.setText("1!Qqwerty")
    }

    override fun onStop() {
        super.onStop()
        connectionErrorSnackBar?.dismiss()
    }



}

