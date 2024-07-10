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
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.android_level_3.constants.Const
import com.example.android_level_3.MainActivity
import com.example.android_level_3.R
import com.example.android_level_3.constants.PreferencesConst
import com.example.android_level_3.databinding.ActivityAuthorizationBinding
import com.example.android_level_3.retrofit.model.UserData
import com.example.android_level_3.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class AuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorizationBinding
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var viewModel: SharedViewModel

    private var connectionErrorSnackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        sharedPreferences = getSharedPreferences(PreferencesConst.PREFERENCES_SETTINGS, MODE_PRIVATE)

        autoLoginCheck()
        setObservers()
        customSymbolTextInputForm()
        emailFormObserver()
        passwordFormObserver()
        setListeners()
    }

    private fun setObservers() {
        viewModel.isVisibleProgressBarInAuthorizationActivity.observe(this) { visibility ->
            if (visibility) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }
        // message when the Internet is disconnected or the request time is long
        viewModel.authorisationResult.observe(this) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackBar = createSnackBar()
                connectionErrorSnackBar?.show()
            } else if (serverResponse.isSuccessful) {
                // save data in SharedPreferences for autologin (if checkbox be checked)
                checkingAutoLoginFunction()
                // save incoming data AccessToken, RefreshToken, UserData
                saveUserDataToPreferences(serverResponse.body()?.data)

                val intent = Intent(this@AuthorizationActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.horiz_from_right_to_center,
                    R.anim.horiz_from_center_to_left)
            } else {
                // if user with this e-mail and password not found, propose to create new user
                createAlertDialog().show()
            }
        }
    }

    // checking records in SharedPreferences, where key is "email"
    // if find, put a “tag” that autologin is required and go to profile page
    private fun autoLoginCheck() {
        if (sharedPreferences?.contains(PreferencesConst.PREFERENCES_EMAIL) == true) {
            sharedPreferences?.edit()?.apply {
                putBoolean(PreferencesConst.PREFERENCES_AUTOLOGIN, true)
            }?.apply()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // go to MainActivity, if registration be successful
    private fun setListeners() {
        binding.btnAuthorizationRegister.setOnClickListener {
            if (emailValidator() && passwordValidator() == getString(R.string.response_ok)) {

                viewModel.getAuthorisation( email = binding.textInputEmailForm.text.toString(),
                    password = binding.textInputPasswordForm.text.toString(),
                    progressBar = viewModel.isVisibleProgressBarInAuthorizationActivity)
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
            putString(PreferencesConst.PREFERENCES_ACCESS_TOKEN, responseData?.accessToken)
            putString(PreferencesConst.PREFERENCES_REFRESH_TOKEN, responseData?.refreshToken)
            putString(PreferencesConst.PREFERENCES_USER_NAME, responseData?.user?.name)
            putString(PreferencesConst.PREFERENCES_USER_CAREER, responseData?.user?.career)
            putString(PreferencesConst.PREFERENCES_USER_ADDRESS, responseData?.user?.address)
            putInt(PreferencesConst.PREFERENCES_USER_ID, responseData?.user?.id!!)
        }?.apply()
    }

    // if checked checkbox "Remember Me", recording authorization data in SharedPreferences
    private fun checkingAutoLoginFunction() {
        if (binding.checkBoxAuthorizationRememberMe.isChecked) {
            sharedPreferences?.edit()?.apply {
                putString(PreferencesConst.PREFERENCES_EMAIL, binding.textInputEmailForm.text.toString())
                putString(PreferencesConst.PREFERENCES_PASSWORD, binding.textInputPasswordForm.text.toString())
            }?.apply()
        }
    }

    // dialog for new user creating
    private fun createAlertDialog(): AlertDialog.Builder {
        return AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.authorisation_alert_dialog_title))
            setMessage(getString(R.string.authorisation_alert_dialog_help_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.authorisation_alert_dialog_positive_button_text)) { dialog, id ->
                val intent = Intent(this@AuthorizationActivity, RegistrationActivity::class.java)
                // transmit email, password, checkBox status to register a new user
                    intent.putExtra(Const.EMAIL, binding.textInputEmailForm.text.toString())
                    intent.putExtra(Const.PASSWORD, binding.textInputPasswordForm.text.toString())
                if (binding.checkBoxAuthorizationRememberMe.isChecked) {
                    intent.putExtra(PreferencesConst.PREFERENCES_CHECKBOX, true)
                } else {
                    intent.putExtra(PreferencesConst.PREFERENCES_CHECKBOX, false)
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

    // tracking changes in e-mail field, followed by validation
    private fun emailFormObserver() {
        binding.textInputEmailForm.doOnTextChanged { _, _, _, _ ->
            if (!emailValidator()) { binding.textInputEmailContainer.helperText =
                getString(R.string.response_wrong_email)
                binding.textInputEmailContainer.setHelperTextColor( ColorStateList
                    .valueOf(getColor(R.color.orange_color)) )
            } else {
                binding.textInputEmailContainer.helperText = getString(R.string.response_ok)
                binding.textInputEmailContainer.setHelperTextColor(ColorStateList.valueOf(Color.GREEN))
            }
        }
    }

    // validation for email text field
    private fun emailValidator(): Boolean {
        val email = binding.textInputEmailForm.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        return true
    }

    // tracking and validation the password field
    private fun passwordFormObserver() {
        binding.textInputPasswordForm.doOnTextChanged { _, _, _, _ ->
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
    }

    // validation entered text for email field
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

    // replacing the standard password escape character with a large character ('●' '⬤')
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

    // save state email and password field (when rotated)
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString(Const.STATE_EMAIL_FIELD, binding.textInputEmailForm.text.toString())
            putString(Const.STATE_PASSWORD_FIELD, binding.textInputPasswordForm.text.toString())
            putBoolean(Const.STATE_CHECKBOX, binding.checkBoxAuthorizationRememberMe.isChecked)
        }
        super.onSaveInstanceState(outState)
    }

    // restore state email and password field (after rotate)
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            binding.textInputEmailForm.setText(getString(Const.STATE_EMAIL_FIELD))
            binding.textInputPasswordForm.setText(getString(Const.STATE_PASSWORD_FIELD))
            binding.checkBoxAuthorizationRememberMe.isChecked = getBoolean(Const.STATE_CHECKBOX)
        }
    }

    // message about internet trouble or long request time
    private fun createSnackBar(): Snackbar {
        return Snackbar.make(binding.root,
            getString(R.string.connection_error_snackbar_message), Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(getColor(R.color.orange_color))
            .setAction(getString(R.string.connection_error_snackbar_action_button_text)) {
                viewModel.getAuthorisation( email = binding.textInputEmailForm.text.toString(),
                    password = binding.textInputPasswordForm.text.toString(),
                    progressBar = viewModel.isVisibleProgressBarInAuthorizationActivity)
            }
    }

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

