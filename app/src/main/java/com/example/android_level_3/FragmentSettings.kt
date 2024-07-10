package com.example.android_level_3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.android_level_3.constants.PreferencesConst
import com.example.android_level_3.databinding.FragmentSettingsBinding
import com.example.android_level_3.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar

class FragmentSettings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel: SharedViewModel by activityViewModels()
    private val sharedPreferences by lazy {
        requireActivity().getSharedPreferences(
            PreferencesConst.PREFERENCES_SETTINGS,
            AppCompatActivity.MODE_PRIVATE)
    }

    private var connectionErrorSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

//      стало не нужным (в задании уроаня 4) ввиду использования TabLayout (пока оставить)
//        binding.btnSettingsViewContacts.setOnClickListener {
//            findNavController().navigate(R.id.action_fragmentSettings_to_fragmentContactsList)
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUserDataToUI()
        setObservers()
        setButtonListener()

        if (sharedPreferences?.getBoolean(PreferencesConst.PREFERENCES_AUTOLOGIN, false) == true) {
            refreshUserDataAfterAutoLogin()
        }
    }

    private fun setObservers() {

        viewModel.isVisibleProgressBarInFragmentSettings.observe(viewLifecycleOwner) { visibility ->
            if (visibility) binding.progressBar.visibility = View.VISIBLE
            else binding.progressBar.visibility = View.GONE
        }

        viewModel.authorisationResult.observe(viewLifecycleOwner) { serverResponse ->
            if (serverResponse == null) {                                                           // показываем Snackbar с ошибкой и даем возможность перезапустить
                connectionErrorSnackbar = createErrorConnectionSnackBar()
                connectionErrorSnackbar?.show()
            } else if (serverResponse.isSuccessful) {

                // сохраняем обновленные данные AccessToken и RefreshToken (после авторизации)
                sharedPreferences.edit().apply {
                    putString(PreferencesConst.PREFERENCES_ACCESS_TOKEN, serverResponse.body()?.data?.accessToken.toString())
                    putString(PreferencesConst.PREFERENCES_REFRESH_TOKEN, serverResponse.body()?.data?.refreshToken.toString())
                }.apply()

                viewModel.token.value = "Bearer ${serverResponse.body()?.data?.accessToken}"
                viewModel.userId.value = serverResponse.body()?.data?.user?.id

            } else {
                // TODO - возможно вместо авторизации делать REFRESH TOKEN (на какой ответ ориентироваться?)
                connectionErrorSnackbar = createErrorConnectionSnackBar()
                connectionErrorSnackbar?.show()
            }
        }
    }

    // авторизация, если был "автологин"
    private fun refreshUserDataAfterAutoLogin() {
        viewModel.getAuthorisation(
            email = sharedPreferences?.getString(PreferencesConst.PREFERENCES_EMAIL, null).toString(),
            password = sharedPreferences.getString(PreferencesConst.PREFERENCES_PASSWORD, null).toString(),
            progressBar = viewModel.isVisibleProgressBarInFragmentSettings)
    }

    private fun createErrorConnectionSnackBar(): Snackbar {
        return Snackbar.make(binding.root,
            getString(R.string.connection_error_snackbar_message), Snackbar.LENGTH_INDEFINITE)
            .setActionTextColor(requireActivity().getColor(R.color.orange_color))
            .setAction(getString(R.string.connection_error_snackbar_action_button_text)) {
                refreshUserDataAfterAutoLogin()
            }
    }

    // подстановка данных о пользователе (при автологине)
    private fun setUserDataToUI() {
        with(binding) {
            sharedPreferences.getString(PreferencesConst.PREFERENCES_USER_NAME, null)?.let {tvProfileName.text = it}
            sharedPreferences.getString(PreferencesConst.PREFERENCES_USER_CAREER, null)?.let { tvProfileProfession.text = it }
            sharedPreferences.getString(PreferencesConst.PREFERENCES_USER_ADDRESS, null)?.let { tvProfileAddress.text = it }
            viewModel.token.value = "Bearer ${sharedPreferences.getString(PreferencesConst.PREFERENCES_ACCESS_TOKEN, "")}"
            viewModel.userId.value = sharedPreferences.getInt(PreferencesConst.PREFERENCES_USER_ID, 0)
        }
    }

    private fun setButtonListener() {

        // выход на страницу авторизации по кнопке Logout + очистка от функции "автологина"
        binding.btnMyProfileLogOut.setOnClickListener {
            sharedPreferences.edit()?.apply {
                clear()
            }?.apply()
            requireActivity().finish()
        }
    }

}