package com.example.android_level_3.presentation.ui.authorization

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.example.android_level_3.R
import com.example.android_level_3.data.retrofit.model.CreateUserModel
import com.example.android_level_3.databinding.ActivityRegistrationBinding
import com.example.android_level_3.domain.constants.Const
import com.example.android_level_3.presentation.ui.MainActivity
import com.example.android_level_3.presentation.ui.authorization.Helper
import com.example.android_level_3.presentation.ui.viewmodel.SharedViewModelFactory
import com.example.android_level_3.presentation.utils.ext.gone
import com.example.android_level_3.presentation.utils.ext.invisible
import com.example.android_level_3.presentation.utils.ext.invisibleIf
import com.example.android_level_3.presentation.utils.ext.visible
import com.example.android_level_3.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel: SharedViewModel by viewModels { SharedViewModelFactory(this) }
    private lateinit var addContactImageResult: ActivityResultLauncher<Intent>

    private var email: String? = null
    private var password: String? = null
    private var autoLogin: Boolean? = null

    private var connectionErrorSnackBar: Snackbar? = null
    private var newUser: CreateUserModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let { getDataFromIntent(it) }

        setActivityResultContract()
        setEditTextListeners()
        setObservers()

        // hide the automatically pop-up keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun getDataFromIntent(intent: Intent) {
        email = intent.getStringExtra(Const.EMAIL) ?: Const.EMAIL_DEFAULT_VALUE
        password = intent.getStringExtra(Const.PASSWORD) ?: Const.PASSWORD_DEFAULT_VALUE
        autoLogin = intent.getBooleanExtra(Const.CHECKBOX_STATUS, false)

    }

    private fun setObservers() {
        viewModel.registrationResult.observe(this) { serverResponse ->
            if (serverResponse == null) {
                connectionErrorSnackBar = createSnackBar()
                connectionErrorSnackBar?.show()
            } else if (serverResponse.isSuccessful) {

                // save authorisation data to Preferences
                viewModel.dataStorage.saveUserDataToPreferences(
                    responseData = serverResponse.body()?.data,
                    autoLogin = autoLogin, email = email, password = password)

                // go to USER profile
                val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                connectionErrorSnackBar = createSnackBar()
                connectionErrorSnackBar?.show()
            }
        }

        viewModel.isVisibleProgressBarInRegistrationActivity.observe(this) { visibility ->
            binding.registrationProgressBar.invisibleIf(visibility)
        }
    }

    // registration (execution) of the “contract” after executing startActivityForResult
    // with receiving data (picture selected from the gallery)
    private fun setActivityResultContract() {
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
                        binding.tvBirthdayHelp.visible()
                        binding.tvBirthdayHelp.setTextColor(getColor(R.color.orange_color))
                    }
                } else {
                    if (binding.etBirthday.text?.isEmpty() == true || binding.etBirthday.text?.isBlank() == true) {
                        binding.etBirthday.hint =
                            getString(R.string.dialog_add_contact_birthday_hint)
                        binding.tvBirthdayHelp.invisible()
                    }
                }
            }
        }

        // check DATE format for BIRTHDAY field
        binding.etBirthday.doOnTextChanged { text, _, _, _ ->
            if (Helper.dateValidator(text.toString()) == null) {
                binding.tvBirthdayHelp.visible()
                binding.tvBirthdayHelp.setTextColor(Color.GREEN)
                binding.tvBirthdayHelp.text =
                    getString(R.string.date_filed_correct_input_message)
            } else {
                binding.tvBirthdayHelp.visible()
                binding.tvBirthdayHelp.setTextColor(getColor(R.color.orange_color))
                binding.tvBirthdayHelp.text = Helper.dateValidator(text.toString())
            }
        }

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

                newUser = CreateUserModel(email = email!!,
                    password = password!!,
                    name = name.toString(),
                    phone = phone.toString(),
                    address = address.toString(),
                    career = career.toString(),
                    birthday = date,
                    image = null)

                // make request to register a new user
                viewModel.registerNewUser(
                    newUserData = newUser!!,
                    progressBar = viewModel.isVisibleProgressBarInRegistrationActivity
                )
            }
        }

        // TODO - only for quick test (DELETE after test)
        binding.imgFillAllDataFields.setOnClickListener {
            with(binding) {
                etUserName.setText("Polina LiveDatova")
                etCareer.setText("Director")
                etPhone.setText("050-555-66-77")
                etAddress.setText("Ukraine, Lviv, Peremoga street, 17")
                etBirthday.setText("2004/10/22")
            }
        }
    }

    // information message about connection error
    private fun createSnackBar(): Snackbar {
        return Snackbar.make(binding.root,
            getString(R.string.connection_error_snackbar_message), Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(getColor(R.color.orange_color))
            .setAction(getString(R.string.connection_error_snackbar_action_button_text)) {
                viewModel.registerNewUser(
                    newUserData = newUser!!,
                    progressBar = viewModel.isVisibleProgressBarInRegistrationActivity
                )
            }
    }

}