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
import com.example.android_level_3.R
import com.example.android_level_3.databinding.ActivityRegistrationBinding
import java.util.Calendar
import java.util.Date

// TODO семнить стартовую активити на AuthorizationActivity

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var addContactImageResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActivityResultContract()
        setEditTextListeners()

        // скрываем автоматически всплывающую клавиатуру
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

    }

    private fun setActivityResultContract() {
        // регистрация (выполнение) "контракта" после отработки startActivityForResult с получением данных (выбранной из галереи картинки)
        addContactImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback {
            if (it.resultCode == RESULT_OK) {
                val avatarImageUri = it?.data?.data
                binding.imgAvatar.setImageURI(avatarImageUri)
            }
        })
    }

    private fun setEditTextListeners() {
        // hide and show HINT on click for PHONE field
        binding.etPhone.onFocusChangeListener = object :OnFocusChangeListener {
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
        binding.etBirthday.onFocusChangeListener = object :OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    if (binding.etBirthday.text?.isEmpty() == true || binding.etBirthday.text?.isBlank() == true) {
                        binding.etBirthday.hint = " "
                        binding.tvBirthdayHelp.visibility = View.VISIBLE
                        binding.tvBirthdayHelp.setTextColor(getColor(R.color.orange_color))
                    }
                } else {
                    if (binding.etBirthday.text?.isEmpty() == true || binding.etBirthday.text?.isBlank() == true) {
                        binding.etBirthday.hint = getString(R.string.dialog_add_contact_birthday_hint)
                        binding.tvBirthdayHelp.visibility = View.INVISIBLE
                    }
                }
            }
        }
        // check DATE format for BIRTHDAY field
        binding.etBirthday.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
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
                        Toast.makeText(this@RegistrationActivity,
                            getString(R.string.registration_empty_field_username), Toast.LENGTH_SHORT).show()
                        return@with
                    }
                }

                val career = Helper.dataFieldValidator(etCareer.text.toString())
                val phone = Helper.dataFieldValidator(etPhone.text.toString())
                val address = Helper.dataFieldValidator(etAddress.text.toString())
                val birthday = Helper.dataFieldValidator(etBirthday.text.toString())
                val date = birthday?.let {
                    if (Helper.dateValidator(it) != null) {
                        Toast.makeText(this@RegistrationActivity,
                            getString(R.string.registration_wrong_birthday_format), Toast.LENGTH_SHORT).show()
                        return@with
                    } else {
                        Helper.getBirthday(it)
                    }
                }
                Log.d("TAG", "name = $name \ncareer = $career \nphone = $phone \naddress = $address \nbirthday = $birthday [${birthday?.javaClass}] \ndate = $date [${date?.javaClass}] \nimage = $image")
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

}